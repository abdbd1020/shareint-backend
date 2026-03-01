package com.shareint.backend.modules.booking.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.booking.dto.BookingDTO;
import com.shareint.backend.modules.booking.dto.CreateBookingRequest;
import com.shareint.backend.modules.booking.dto.RecordPaymentRequest;
import com.shareint.backend.modules.booking.model.Booking;
import com.shareint.backend.modules.booking.repository.BookingRepository;
import com.shareint.backend.modules.trip.model.Trip;
import com.shareint.backend.modules.trip.model.TripSeat;
import com.shareint.backend.modules.trip.repository.TripRepository;
import com.shareint.backend.modules.trip.repository.TripSeatRepository;
import com.shareint.backend.modules.notification.service.NotificationService;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.10"); // 10%

    @Transactional
    public BookingDTO createBooking(String passengerPhoneNumber, CreateBookingRequest request) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getStatus() != Trip.TripStatus.SCHEDULED) {
            throw new IllegalArgumentException("Trip is not available for booking");
        }

        if (trip.getDriver().getId().equals(passenger.getId())) {
            throw new IllegalArgumentException("Driver cannot book their own trip");
        }

        int requestedSeats = request.getSeatsCount();
        if (trip.getAvailableSeats() < requestedSeats) {
            throw new IllegalArgumentException("Not enough available seats on this trip");
        }

        BigDecimal totalSeatPrice = trip.getPricePerSeat().multiply(new BigDecimal(requestedSeats));
        BigDecimal platformFee = totalSeatPrice.multiply(PLATFORM_FEE_PERCENTAGE);

        // Create booking with CONFIRMED status — no upfront payment required
        Booking booking = Booking.builder()
                .trip(trip)
                .passenger(passenger)
                .totalSeatPrice(totalSeatPrice)
                .platformFee(platformFee)
                .bookedSeatsCount(requestedSeats)
                .status(Booking.BookingStatus.CONFIRMED)
                .creator(passenger)
                .updater(passenger)
                .build();

        booking = bookingRepository.save(booking);

        // Auto-generate seat identifiers (no named seat mapping needed)
        List<String> seatLabels = new ArrayList<>();
        String bookingPrefix = booking.getId().toString().substring(0, 8);
        for (int i = 1; i <= requestedSeats; i++) {
            String label = "seat-" + bookingPrefix + "-" + i;
            TripSeat seat = TripSeat.builder()
                    .trip(trip)
                    .seatIdentifier(label)
                    .isBooked(true)
                    .booking(booking)
                    .creator(passenger)
                    .updater(passenger)
                    .build();
            tripSeatRepository.save(seat);
            seatLabels.add(label);
        }

        trip.setAvailableSeats(trip.getAvailableSeats() - requestedSeats);
        tripRepository.save(trip);

        notificationService.sendPushNotification(
                trip.getDriver().getId(),
                "New Booking — " + passenger.getFullName(),
                requestedSeats + " seat(s) booked on your " +
                        (trip.getOrigin() != null ? trip.getOrigin().getNameEn() : "") +
                        " → " +
                        (trip.getDestination() != null ? trip.getDestination().getNameEn() : "") +
                        " trip. Phone: " + passenger.getPhoneNumber()
        );

        return mapToDTO(booking, seatLabels);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBooking(String passengerPhoneNumber, UUID bookingId) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        Booking booking = bookingRepository.findByIdAndPassengerId(bookingId, passenger.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return mapToDTOWithSeats(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getMyBookings(String passengerPhoneNumber) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        return bookingRepository.findByPassengerIdOrderByCreatedAtDesc(passenger.getId())
                .stream()
                .map(this::mapToDTOWithSeats)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getDriverBookings(String driverPhoneNumber) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        return bookingRepository.findByTripDriverIdOrderByCreatedAtDesc(driver.getId())
                .stream()
                .map(this::mapToDTOWithSeats)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO recordPayment(String passengerPhoneNumber, UUID bookingId, RecordPaymentRequest request) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        Booking booking = bookingRepository.findByIdAndPassengerId(bookingId, passenger.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot record payment for a cancelled booking");
        }

        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setUpdatedAt(Instant.now());
        booking = bookingRepository.save(booking);

        return mapToDTOWithSeats(booking);
    }

    @Transactional
    public BookingDTO markCommissionPaid(String driverPhoneNumber, UUID bookingId) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getTrip().getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("This booking does not belong to your trip");
        }

        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Can only mark commission paid for completed bookings");
        }

        booking.setCommissionPaidAt(Instant.now());
        booking.setUpdatedAt(Instant.now());
        booking = bookingRepository.save(booking);

        return mapToDTOWithSeats(booking);
    }

    @Transactional
    public BookingDTO cancelBooking(String passengerPhoneNumber, UUID bookingId) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getPassenger().getId().equals(passenger.getId())) {
            throw new IllegalArgumentException("Booking does not belong to this passenger");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed booking");
        }

        List<TripSeat> seats = tripSeatRepository.findByTripId(booking.getTrip().getId())
                .stream()
                .filter(seat -> bookingId.equals(seat.getBooking() != null ? seat.getBooking().getId() : null))
                .collect(Collectors.toList());

        for (TripSeat seat : seats) {
            seat.setBooked(false);
            seat.setBooking(null);
            tripSeatRepository.save(seat);
        }

        Trip trip = booking.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + booking.getBookedSeatsCount());
        tripRepository.save(trip);

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        booking = bookingRepository.save(booking);

        return mapToDTO(booking, seats.stream().map(TripSeat::getSeatIdentifier).collect(Collectors.toList()));
    }

    private BookingDTO mapToDTOWithSeats(Booking booking) {
        List<String> seats = tripSeatRepository.findByTripId(booking.getTrip().getId())
                .stream()
                .filter(seat -> booking.getId().equals(seat.getBooking() != null ? seat.getBooking().getId() : null))
                .map(TripSeat::getSeatIdentifier)
                .collect(Collectors.toList());

        return mapToDTO(booking, seats);
    }

    private BookingDTO mapToDTO(Booking booking, List<String> seats) {
        BigDecimal totalCharged = booking.getTotalSeatPrice().add(booking.getPlatformFee());
        Trip trip = booking.getTrip();
        User passenger = booking.getPassenger();
        User driver = trip.getDriver();

        return BookingDTO.builder()
                .id(booking.getId())
                .tripId(trip.getId())
                .passengerId(passenger.getId())
                .passengerName(passenger.getFullName())
                .passengerPhone(passenger.getPhoneNumber())
                .driverName(driver.getFullName())
                .driverPhone(driver.getPhoneNumber())
                .originName(trip.getOrigin() != null ? trip.getOrigin().getNameEn() : null)
                .destinationName(trip.getDestination() != null ? trip.getDestination().getNameEn() : null)
                .departureTime(trip.getDepartureTime())
                .pricePerSeat(trip.getPricePerSeat())
                .totalSeatPrice(booking.getTotalSeatPrice())
                .platformFee(booking.getPlatformFee())
                .totalCharged(totalCharged)
                .bookedSeatsCount(booking.getBookedSeatsCount())
                .seatIdentifiers(seats)
                .status(booking.getStatus())
                .paymentMethod(booking.getPaymentMethod())
                .commissionPaidAt(booking.getCommissionPaidAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

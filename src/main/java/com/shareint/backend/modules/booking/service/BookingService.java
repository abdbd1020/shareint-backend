package com.shareint.backend.modules.booking.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.booking.dto.BookingDTO;
import com.shareint.backend.modules.booking.dto.CreateBookingRequest;
import com.shareint.backend.modules.booking.model.Booking;
import com.shareint.backend.modules.booking.repository.BookingRepository;
import com.shareint.backend.modules.trip.model.Trip;
import com.shareint.backend.modules.trip.model.TripSeat;
import com.shareint.backend.modules.trip.repository.TripRepository;
import com.shareint.backend.modules.trip.repository.TripSeatRepository;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.05"); // 5%

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

        int requestedSeats = request.getSeatIdentifiers().size();
        if (trip.getAvailableSeats() < requestedSeats) {
            throw new IllegalArgumentException("Not enough available seats on this trip");
        }

        // Process seats
        List<TripSeat> reservedSeats = new ArrayList<>();
        for (String seatId : request.getSeatIdentifiers()) {
            TripSeat seat = tripSeatRepository.findByTripIdAndSeatIdentifier(trip.getId(), seatId)
                    .orElseGet(() -> TripSeat.builder()
                            .trip(trip)
                            .seatIdentifier(seatId)
                            .isBooked(false)
                            .creator(passenger)
                            .updater(passenger)
                            .build());

            if (seat.isBooked()) {
                throw new IllegalArgumentException("Seat " + seatId + " is already booked");
            }
            
            seat.setBooked(true);
            reservedSeats.add(seat);
        }

        // Calculate costs
        BigDecimal totalSeatPrice = trip.getPricePerSeat().multiply(new BigDecimal(requestedSeats));
        BigDecimal platformFee = totalSeatPrice.multiply(PLATFORM_FEE_PERCENTAGE);

        // Create Booking
        Booking booking = Booking.builder()
                .trip(trip)
                .passenger(passenger)
                .totalSeatPrice(totalSeatPrice)
                .platformFee(platformFee)
                .bookedSeatsCount(requestedSeats)
                .status(Booking.BookingStatus.PENDING_PAYMENT)
                .creator(passenger)
                .updater(passenger)
                .build();

        booking = bookingRepository.save(booking);

        // Link seats to booking and save
        for (TripSeat seat : reservedSeats) {
            seat.setBooking(booking);
            tripSeatRepository.save(seat);
        }

        // Update trip available seats
        trip.setAvailableSeats(trip.getAvailableSeats() - requestedSeats);
        tripRepository.save(trip);

        return mapToDTO(booking, request.getSeatIdentifiers());
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

        // 1. Release the seats
        List<TripSeat> seats = tripSeatRepository.findByTripId(booking.getTrip().getId())
                .stream()
                .filter(seat -> bookingId.equals(seat.getBooking() != null ? seat.getBooking().getId() : null))
                .collect(Collectors.toList());

        for (TripSeat seat : seats) {
            seat.setBooked(false);
            seat.setBooking(null);
            tripSeatRepository.save(seat);
        }

        // 2. Restore available seats on the trip
        Trip trip = booking.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + booking.getBookedSeatsCount());
        tripRepository.save(trip);

        // 3. Mark booking as cancelled
        booking.setStatus(Booking.BookingStatus.CANCELLED);
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
        // Since totalCharged is GENERATED ALWAYS AS (totalSeatPrice + platformFee), 
        // it might be null before a flush/refresh. We can manually calculate it for DTO.
        BigDecimal totalCharged = booking.getTotalSeatPrice().add(booking.getPlatformFee());

        return BookingDTO.builder()
                .id(booking.getId())
                .tripId(booking.getTrip().getId())
                .passengerId(booking.getPassenger().getId())
                .passengerName(booking.getPassenger().getFullName())
                .totalSeatPrice(booking.getTotalSeatPrice())
                .platformFee(booking.getPlatformFee())
                .totalCharged(totalCharged)
                .bookedSeatsCount(booking.getBookedSeatsCount())
                .seatIdentifiers(seats)
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}

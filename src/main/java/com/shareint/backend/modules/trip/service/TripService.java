package com.shareint.backend.modules.trip.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.location.model.Location;
import com.shareint.backend.modules.location.repository.LocationRepository;
import com.shareint.backend.modules.trip.dto.CreateTripRequest;
import com.shareint.backend.modules.trip.dto.TripDTO;
import com.shareint.backend.modules.trip.model.Trip;
import com.shareint.backend.modules.trip.model.Vehicle;
import com.shareint.backend.modules.trip.repository.TripRepository;
import com.shareint.backend.modules.trip.repository.VehicleRepository;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public TripDTO publishTrip(String driverPhoneNumber, CreateTripRequest request) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("Vehicle does not belong to the driver");
        }

        if (!vehicle.isApproved()) {
            throw new IllegalArgumentException("Vehicle is not approved yet");
        }

        if (request.getAvailableSeats() > vehicle.getTotalCapacity()) {
            throw new IllegalArgumentException("Available seats cannot exceed vehicle total capacity");
        }

        Location origin = locationRepository.findById(request.getOriginLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin location not found"));

        Location destination = locationRepository.findById(request.getDestinationLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination location not found"));

        Trip trip = Trip.builder()
                .driver(driver)
                .vehicle(vehicle)
                .origin(origin)
                .destination(destination)
                .departureTime(request.getDepartureTime())
                .estimatedArrivalTime(request.getEstimatedArrivalTime())
                .pricePerSeat(request.getPricePerSeat())
                .availableSeats(request.getAvailableSeats())
                .meetingPoint(request.getMeetingPoint())
                .dropOffPoint(request.getDropOffPoint())
                .creator(driver)
                .updater(driver)
                .build();

        trip = tripRepository.save(trip);
        return mapToDTO(trip);
    }

    @Transactional(readOnly = true)
    public List<TripDTO> getMyTrips(String driverPhoneNumber) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        return tripRepository.findByDriverIdOrderByDepartureTimeDesc(driver.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TripDTO cancelTrip(String driverPhoneNumber, UUID tripId) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("You are not authorized to cancel this trip");
        }

        if (trip.getStatus() == Trip.TripStatus.COMPLETED || trip.getStatus() == Trip.TripStatus.CANCELLED) {
            throw new IllegalArgumentException("Trip is already " + trip.getStatus().name());
        }

        // Ideally here we would also refund bookings, handled in a later phase
        trip.setStatus(Trip.TripStatus.CANCELLED);
        trip = tripRepository.save(trip);
        
        return mapToDTO(trip);
    }

    @Transactional(readOnly = true)
    public List<TripDTO> searchTrips(UUID originId, UUID destinationId, Instant dateFrom, Instant dateTo, Integer minSeats) {
        int requiredSeats = minSeats != null ? minSeats : 1;
        
        return tripRepository.searchTrips(originId, destinationId, dateFrom, dateTo, requiredSeats)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TripDTO mapToDTO(Trip trip) {
        return TripDTO.builder()
                .id(trip.getId())
                .driverId(trip.getDriver().getId())
                .driverName(trip.getDriver().getFullName())
                .vehicleId(trip.getVehicle().getId())
                .vehicleModel(trip.getVehicle().getMake() + " " + trip.getVehicle().getModel())
                .originLocationId(trip.getOrigin().getId())
                .originName(trip.getOrigin().getNameEn())
                .destinationLocationId(trip.getDestination().getId())
                .destinationName(trip.getDestination().getNameEn())
                .departureTime(trip.getDepartureTime())
                .estimatedArrivalTime(trip.getEstimatedArrivalTime())
                .pricePerSeat(trip.getPricePerSeat())
                .availableSeats(trip.getAvailableSeats())
                .status(trip.getStatus())
                .meetingPoint(trip.getMeetingPoint())
                .dropOffPoint(trip.getDropOffPoint())
                .build();
    }
}

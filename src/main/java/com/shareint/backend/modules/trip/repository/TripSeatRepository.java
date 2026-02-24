package com.shareint.backend.modules.trip.repository;

import com.shareint.backend.modules.trip.model.TripSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {
    List<TripSeat> findByTripId(UUID tripId);
    Optional<TripSeat> findByTripIdAndSeatIdentifier(UUID tripId, String seatIdentifier);
}

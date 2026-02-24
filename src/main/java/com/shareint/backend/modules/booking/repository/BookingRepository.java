package com.shareint.backend.modules.booking.repository;

import com.shareint.backend.modules.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId);
    List<Booking> findByTripId(UUID tripId);
}

package com.shareint.backend.modules.booking.repository;

import com.shareint.backend.modules.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId);
    List<Booking> findByTripId(UUID tripId);

    Optional<Booking> findByIdAndPassengerId(UUID id, UUID passengerId);

    @Query("SELECT b FROM Booking b WHERE b.trip.driver.id = :driverId ORDER BY b.createdAt DESC")
    List<Booking> findByTripDriverIdOrderByCreatedAtDesc(@Param("driverId") UUID driverId);
}

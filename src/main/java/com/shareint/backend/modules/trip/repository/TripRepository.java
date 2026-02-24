package com.shareint.backend.modules.trip.repository;

import com.shareint.backend.modules.trip.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
    
    List<Trip> findByDriverIdOrderByDepartureTimeDesc(UUID driverId);

    @Query("SELECT t FROM Trip t WHERE t.status = 'SCHEDULED' " +
           "AND t.availableSeats >= :minSeats " +
           "AND (:originId IS NULL OR t.origin.id = :originId) " +
           "AND (:destId IS NULL OR t.destination.id = :destId) " +
           "AND (cast(:dateFrom as timestamp) IS NULL OR t.departureTime >= :dateFrom) " +
           "AND (cast(:dateTo as timestamp) IS NULL OR t.departureTime <= :dateTo) " +
           "ORDER BY t.departureTime ASC")
    List<Trip> searchTrips(
            @Param("originId") UUID originLocationId,
            @Param("destId") UUID destinationLocationId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("minSeats") Integer minAvailableSeats
    );
}

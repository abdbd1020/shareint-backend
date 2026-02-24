package com.shareint.backend.modules.trip.repository;

import com.shareint.backend.modules.trip.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    List<Vehicle> findByDriverId(UUID driverId);
    boolean existsByLicensePlateIgnoreCase(String licensePlate);
}

package com.shareint.backend.modules.trip.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.trip.dto.CreateVehicleRequest;
import com.shareint.backend.modules.trip.dto.VehicleDTO;
import com.shareint.backend.modules.trip.model.Vehicle;
import com.shareint.backend.modules.trip.repository.VehicleRepository;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public VehicleDTO addVehicle(String driverPhoneNumber, CreateVehicleRequest request) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (vehicleRepository.existsByLicensePlateIgnoreCase(request.getLicensePlate())) {
            throw new IllegalArgumentException("Vehicle with this license plate already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .licensePlate(request.getLicensePlate())
                .color(request.getColor())
                .totalCapacity(request.getTotalCapacity())
                .isApproved(false) // Pending manual admin approval
                .creator(driver)
                .updater(driver)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        return mapToDTO(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleDTO> getMyVehicles(String driverPhoneNumber) {
        User driver = userRepository.findByPhoneNumber(driverPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        return vehicleRepository.findByDriverId(driver.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVehicle(String driverPhoneNumber, UUID vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getDriver().getPhoneNumber().equals(driverPhoneNumber)) {
            throw new IllegalArgumentException("You are not authorized to delete this vehicle");
        }

        // Soft delete based on @SQLRestriction
        vehicle.setDeletedAt(java.time.Instant.now());
        vehicleRepository.save(vehicle);
    }

    private VehicleDTO mapToDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriver().getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .totalCapacity(vehicle.getTotalCapacity())
                .isApproved(vehicle.isApproved())
                .build();
    }
}

package com.shareint.backend.modules.trip.controller;

import com.shareint.backend.modules.trip.dto.CreateVehicleRequest;
import com.shareint.backend.modules.trip.dto.VehicleDTO;
import com.shareint.backend.modules.trip.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/web/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleDTO> addVehicle(
            Authentication authentication,
            @Valid @RequestBody CreateVehicleRequest request) {
        String phoneNumber = authentication.getName();
        VehicleDTO response = vehicleService.addVehicle(phoneNumber, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<VehicleDTO>> getMyVehicles(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(vehicleService.getMyVehicles(phoneNumber));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(
            Authentication authentication,
            @PathVariable UUID id) {
        String phoneNumber = authentication.getName();
        vehicleService.deleteVehicle(phoneNumber, id);
        return ResponseEntity.noContent().build();
    }
}

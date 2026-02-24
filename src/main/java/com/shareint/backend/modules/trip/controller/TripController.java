package com.shareint.backend.modules.trip.controller;

import com.shareint.backend.modules.trip.dto.CreateTripRequest;
import com.shareint.backend.modules.trip.dto.TripDTO;
import com.shareint.backend.modules.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripDTO> publishTrip(
            Authentication authentication,
            @Valid @RequestBody CreateTripRequest request) {
        String phoneNumber = authentication.getName();
        TripDTO response = tripService.publishTrip(phoneNumber, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TripDTO>> getMyTrips(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(tripService.getMyTrips(phoneNumber));
    }

    @PutMapping("/{tripId}/cancel")
    public ResponseEntity<TripDTO> cancelTrip(
            Authentication authentication,
            @PathVariable UUID tripId) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(tripService.cancelTrip(phoneNumber, tripId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TripDTO>> searchTrips(
            @RequestParam(required = false) UUID originId,
            @RequestParam(required = false) UUID destinationId,
            @RequestParam(required = false) java.time.Instant dateFrom,
            @RequestParam(required = false) java.time.Instant dateTo,
            @RequestParam(required = false, defaultValue = "1") Integer minSeats) {
        return ResponseEntity.ok(tripService.searchTrips(originId, destinationId, dateFrom, dateTo, minSeats));
    }
}

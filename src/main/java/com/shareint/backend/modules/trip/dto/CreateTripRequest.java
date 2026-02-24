package com.shareint.backend.modules.trip.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "Origin location ID is required")
    private UUID originLocationId;

    @NotNull(message = "Destination location ID is required")
    private UUID destinationLocationId;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private Instant departureTime;

    private Instant estimatedArrivalTime;

    @NotNull(message = "Price per seat is required")
    @Min(value = 1, message = "Price per seat must be greater than 0")
    private BigDecimal pricePerSeat;

    @NotNull(message = "Available seats are required")
    @Min(value = 1, message = "Available seats must be at least 1")
    private Integer availableSeats;

    private String meetingPoint;

    private String dropOffPoint;
}

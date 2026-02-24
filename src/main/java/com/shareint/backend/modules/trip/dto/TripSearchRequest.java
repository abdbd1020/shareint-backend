package com.shareint.backend.modules.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchRequest {
    private UUID originLocationId;
    private UUID destinationLocationId;
    // Optional params
    private Instant dateFrom;
    private Instant dateTo;
    private Integer minAvailableSeats;
}

package com.shareint.backend.modules.trip.dto;

import com.shareint.backend.modules.trip.model.Trip.TripStatus;
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
public class TripDTO {
    private UUID id;
    private UUID driverId;
    private String driverName;
    private UUID vehicleId;
    private String vehicleModel;
    private UUID originLocationId;
    private String originName;
    private UUID destinationLocationId;
    private String destinationName;
    private Instant departureTime;
    private Instant estimatedArrivalTime;
    private BigDecimal pricePerSeat;
    private Integer availableSeats;
    private TripStatus status;
    private String meetingPoint;
    private String dropOffPoint;
}

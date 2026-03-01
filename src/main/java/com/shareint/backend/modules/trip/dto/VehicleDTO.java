package com.shareint.backend.modules.trip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private UUID id;
    private UUID driverId;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String color;
    private Integer totalCapacity;
    @JsonProperty("isApproved")
    private boolean isApproved;
}

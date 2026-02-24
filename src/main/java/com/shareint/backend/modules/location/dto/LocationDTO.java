package com.shareint.backend.modules.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private UUID id;
    private String nameEn;
    private String nameBn;
    private UUID parentId;
    private boolean isDistanceConsidered;
    private boolean isActive;
}

package com.shareint.backend.modules.user.dto;

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
public class DeviceTokenDTO {
    private UUID id;
    private UUID userId;
    private String fcmToken;
    private String deviceType;
    private Instant createdAt;
    private Instant updatedAt;
}

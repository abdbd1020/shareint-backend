package com.shareint.backend.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveDeviceTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    private String deviceType; // "ANDROID", "IOS", "WEB"
}

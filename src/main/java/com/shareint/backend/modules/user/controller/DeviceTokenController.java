package com.shareint.backend.modules.user.controller;

import com.shareint.backend.modules.user.dto.DeviceTokenDTO;
import com.shareint.backend.modules.user.dto.SaveDeviceTokenRequest;
import com.shareint.backend.modules.user.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/web/v1/users/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<DeviceTokenDTO> saveDeviceToken(
            Authentication authentication,
            @Valid @RequestBody SaveDeviceTokenRequest request) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(deviceTokenService.saveDeviceToken(phoneNumber, request));
    }

    @DeleteMapping("/{fcmToken}")
    public ResponseEntity<Void> deleteDeviceToken(
            Authentication authentication,
            @PathVariable String fcmToken) {
        String phoneNumber = authentication.getName();
        deviceTokenService.deleteDeviceToken(phoneNumber, fcmToken);
        return ResponseEntity.noContent().build();
    }
}

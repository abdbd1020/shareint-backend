package com.shareint.backend.modules.user.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.user.dto.DeviceTokenDTO;
import com.shareint.backend.modules.user.dto.SaveDeviceTokenRequest;
import com.shareint.backend.modules.user.model.DeviceToken;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.DeviceTokenRepository;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceTokenDTO saveDeviceToken(String phoneNumber, SaveDeviceTokenRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<DeviceToken> existingToken = deviceTokenRepository.findByFcmToken(request.getFcmToken());

        DeviceToken deviceToken;
        if (existingToken.isPresent()) {
            deviceToken = existingToken.get();
            // If the token is already registered but to a different user, re-assign it.
            // (happens when users log out and log in as someone else on the same physical device)
            deviceToken.setUser(user);
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setUpdatedAt(Instant.now());
        } else {
            deviceToken = DeviceToken.builder()
                    .user(user)
                    .fcmToken(request.getFcmToken())
                    .deviceType(request.getDeviceType())
                    .build();
        }

        deviceToken = deviceTokenRepository.save(deviceToken);

        return DeviceTokenDTO.builder()
                .id(deviceToken.getId())
                .userId(deviceToken.getUser().getId())
                .fcmToken(deviceToken.getFcmToken())
                .deviceType(deviceToken.getDeviceType())
                .createdAt(deviceToken.getCreatedAt())
                .updatedAt(deviceToken.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteDeviceToken(String phoneNumber, String fcmToken) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        deviceTokenRepository.findByFcmToken(fcmToken).ifPresent(token -> {
            if (token.getUser().getId().equals(user.getId())) {
                deviceTokenRepository.delete(token);
            }
        });
    }
}

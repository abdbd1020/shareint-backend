package com.shareint.backend.modules.user.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.user.dto.UpdateUserProfileRequest;
import com.shareint.backend.modules.user.dto.UserProfileResponse;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileByPhone(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phoneNumber));
        return mapToResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfileByPhone(String phoneNumber, UpdateUserProfileRequest request) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phoneNumber));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getNidNumber() != null) {
            user.setNidNumber(request.getNidNumber());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .nidNumber(user.getNidNumber())
                .avatarUrl(user.getAvatarUrl())
                .averageRating(user.getAverageRating())
                .build();
    }
}

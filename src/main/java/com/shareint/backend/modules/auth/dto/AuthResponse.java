package com.shareint.backend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shareint.backend.modules.user.model.User.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private UserDto user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private UUID id;
        private String phoneNumber;
        private String fullName;
        private String email;
        private RoleType role;

        @JsonProperty("isVerified")
        private boolean isVerified;

        @JsonProperty("isNidVerified")
        private boolean isNidVerified;

        @JsonProperty("isProfileComplete")
        private boolean isProfileComplete;
    }
}

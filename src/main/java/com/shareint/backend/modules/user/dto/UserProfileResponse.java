package com.shareint.backend.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shareint.backend.modules.user.model.User.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String phoneNumber;
    private String fullName;
    private RoleType role;

    // @JsonProperty ensures Jackson serializes this as "isVerified" not "verified"
    @JsonProperty("isVerified")
    private boolean isVerified;

    @JsonProperty("isNidVerified")
    private boolean isNidVerified;

    private String nidPhotoUrl;
    private String nidNumber;
    private String avatarUrl;
    private BigDecimal averageRating;
}

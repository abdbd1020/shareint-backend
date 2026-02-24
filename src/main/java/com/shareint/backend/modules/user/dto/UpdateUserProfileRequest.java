package com.shareint.backend.modules.user.dto;

import com.shareint.backend.modules.user.model.User.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    private String fullName;
    private String nidNumber;
    private String avatarUrl;
    private RoleType role;
}

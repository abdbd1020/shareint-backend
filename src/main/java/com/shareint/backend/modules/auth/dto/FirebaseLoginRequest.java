package com.shareint.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseLoginRequest {

    @NotBlank(message = "Firebase ID token is required")
    private String firebaseIdToken;
}

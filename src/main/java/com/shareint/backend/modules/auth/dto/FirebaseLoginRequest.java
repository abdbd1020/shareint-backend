package com.shareint.backend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseLoginRequest {

    @NotBlank(message = "Firebase ID token is required")
    @JsonProperty("firebaseIdToken")
    @JsonAlias({"idToken", "id_token", "firebase_id_token"})
    private String firebaseIdToken;
}

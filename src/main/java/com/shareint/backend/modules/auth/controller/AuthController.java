package com.shareint.backend.modules.auth.controller;

import com.shareint.backend.core.dto.BaseResponse;
import com.shareint.backend.modules.auth.dto.AuthResponse;
import com.shareint.backend.modules.auth.dto.FirebaseLoginRequest;
import com.shareint.backend.modules.auth.dto.SendOtpRequest;
import com.shareint.backend.modules.auth.dto.VerifyOtpRequest;
import com.shareint.backend.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<BaseResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request.getPhoneNumber());
        return ResponseEntity.ok(BaseResponse.success(null, "OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<BaseResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request.getPhoneNumber(), request.getOtp());
        return ResponseEntity.ok(BaseResponse.success(response, "Authentication successful"));
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<BaseResponse<AuthResponse>> firebaseLogin(@Valid @RequestBody FirebaseLoginRequest request) {
        AuthResponse response = authService.verifyFirebaseToken(request.getFirebaseIdToken());
        return ResponseEntity.ok(BaseResponse.success(response, "Firebase authentication successful"));
    }
}

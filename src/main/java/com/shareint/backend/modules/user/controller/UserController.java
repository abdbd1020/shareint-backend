package com.shareint.backend.modules.user.controller;

import com.shareint.backend.core.dto.BaseResponse;
import com.shareint.backend.modules.auth.dto.SubmitDriverDocumentsRequest;
import com.shareint.backend.modules.user.dto.DriverDocumentDTO;
import com.shareint.backend.modules.user.dto.UpdateUserProfileRequest;
import com.shareint.backend.modules.user.dto.UserProfileResponse;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import com.shareint.backend.modules.user.service.DriverDocumentService;
import com.shareint.backend.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DriverDocumentService driverDocumentService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfileByPhone(phoneNumber));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(userService.updateUserProfileByPhone(phoneNumber, request));
    }

    @PostMapping("/ratings")
    public ResponseEntity<com.shareint.backend.modules.user.dto.RatingDTO> submitRating(
            Authentication authentication,
            @Valid @RequestBody com.shareint.backend.modules.user.dto.CreateRatingRequest request,
            @org.springframework.beans.factory.annotation.Autowired com.shareint.backend.modules.user.service.RatingService ratingService) {
        String phoneNumber = authentication.getName();
        return new ResponseEntity<>(ratingService.submitRating(phoneNumber, request), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/ratings")
    public ResponseEntity<java.util.List<com.shareint.backend.modules.user.dto.RatingDTO>> getUserRatings(
            @PathVariable UUID userId,
            @org.springframework.beans.factory.annotation.Autowired com.shareint.backend.modules.user.service.RatingService ratingService) {
        return ResponseEntity.ok(ratingService.getUserRatings(userId));
    }

    @PostMapping("/me/driver-documents")
    public ResponseEntity<BaseResponse<List<DriverDocumentDTO>>> submitDriverDocuments(
            Authentication authentication,
            @Valid @RequestBody SubmitDriverDocumentsRequest request) {
        String phoneNumber = authentication.getName();
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new com.shareint.backend.core.exception.ResourceNotFoundException("User not found"));
        List<DriverDocumentDTO> documents = driverDocumentService.submitDocuments(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(documents, "Documents submitted for review"));
    }

    @GetMapping("/me/driver-documents")
    public ResponseEntity<BaseResponse<List<DriverDocumentDTO>>> getDriverDocumentStatus(
            Authentication authentication) {
        String phoneNumber = authentication.getName();
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new com.shareint.backend.core.exception.ResourceNotFoundException("User not found"));
        List<DriverDocumentDTO> documents = driverDocumentService.getStatus(user.getId());
        return ResponseEntity.ok(BaseResponse.success(documents, "OK"));
    }
}

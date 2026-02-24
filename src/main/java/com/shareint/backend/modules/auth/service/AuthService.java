package com.shareint.backend.modules.auth.service;

import com.shareint.backend.core.exception.ApiException;
import com.shareint.backend.core.security.JwtService;
import com.shareint.backend.modules.auth.dto.AuthResponse;
import com.shareint.backend.modules.auth.dto.AuthResponse.UserDto;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    // In-memory OTP cache for MVP. In Production, replace with Redis template mapping Phone -> OTP!
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    public void sendOtp(String phoneNumber) {
        // Generate 4-digit OTP
        String otp = String.format("%04d", new Random().nextInt(10000));
        
        // Save to cache (expires automatically in real Redis)
        otpCache.put(phoneNumber, otp);
        
        // TODO: Integrate actual SMS Gateway here (e.g., Grameenphone SMS API, Twilio, or SSLWireless)
        log.info("🔐 DEVELOPMENT ONLY: OTP for {} is {}", phoneNumber, otp);
    }

    public AuthResponse verifyOtp(String phoneNumber, String otp) {
        String cachedOtp = otpCache.get(phoneNumber);
        
        // Special bypass for Apple/Google Play reviewers or static test numbers
        boolean isStaticTestAccount = phoneNumber.equals("+8801700000000") && otp.equals("1234");

        if (!isStaticTestAccount) {
            if (cachedOtp == null) {
                throw new ApiException("OTP expired or not requested", HttpStatus.BAD_REQUEST);
            }
            if (!cachedOtp.equals(otp)) {
                throw new ApiException("Invalid OTP", HttpStatus.UNAUTHORIZED);
            }
        }

        // Clean up OTP cache
        otpCache.remove(phoneNumber);

        // Fetch user or register new passenger
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = User.builder()
                    .phoneNumber(phoneNumber)
                    .role(User.RoleType.PASSENGER)
                    .isVerified(false)
                    .build();
            user = userRepository.save(user);
        }

        // Generate JWT
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(UserDto.builder()
                        .id(user.getId())
                        .phoneNumber(user.getPhoneNumber())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .isVerified(user.isVerified())
                        .build())
                .build();
    }

    /**
     * Verifies a Firebase ID token (from client-side Firebase Phone Auth),
     * extracts the phone number, finds or creates the user, and issues a custom JWT.
     */
    public AuthResponse verifyFirebaseToken(String firebaseIdToken) {
        try {
            // 1. Verify the Firebase ID token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseIdToken);
            
            // 2. Extract phone number from Firebase token
            String phoneNumber = (String) decodedToken.getClaims().get("phone_number");
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new ApiException("Firebase token does not contain a phone number", HttpStatus.BAD_REQUEST);
            }

            log.info("Firebase Phone Auth verified for: {}", phoneNumber);

            // 3. Find or create user (same logic as OTP verify)
            Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
            User user;

            if (userOptional.isPresent()) {
                user = userOptional.get();
            } else {
                user = User.builder()
                        .phoneNumber(phoneNumber)
                        .role(User.RoleType.PASSENGER)
                        .isVerified(true) // Firebase-verified phone = trusted
                        .build();
                user = userRepository.save(user);
            }

            // 4. Generate our own JWT
            String jwtToken = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .user(UserDto.builder()
                            .id(user.getId())
                            .phoneNumber(user.getPhoneNumber())
                            .fullName(user.getFullName())
                            .role(user.getRole())
                            .isVerified(user.isVerified())
                            .build())
                    .build();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Firebase token verification failed", e);
            throw new ApiException("Invalid or expired Firebase token", HttpStatus.UNAUTHORIZED);
        }
    }
}

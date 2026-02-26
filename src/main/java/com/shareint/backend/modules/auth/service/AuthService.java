package com.shareint.backend.modules.auth.service;

import com.shareint.backend.core.exception.ApiException;
import com.shareint.backend.core.security.JwtService;
import com.shareint.backend.modules.auth.dto.AuthResponse;
import com.shareint.backend.modules.auth.dto.AuthResponse.UserDto;
import com.shareint.backend.modules.auth.dto.CompleteProfileRequest;
import com.shareint.backend.modules.auth.dto.LoginRequest;
import com.shareint.backend.modules.auth.dto.SignUpRequest;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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
                .user(buildUserDto(user))
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
                        .isProfileComplete(false) // New users must complete profile
                        .build();
                user = userRepository.save(user);
            }

            // 4. Generate our own JWT
            String jwtToken = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .user(buildUserDto(user))
                    .build();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Firebase token verification failed", e);
            throw new ApiException("Invalid or expired Firebase token", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Sign up a new user with Firebase ID token + profile details.
     * Verifies the Firebase token, creates a new user, hashes password, and issues JWT.
     */
    public AuthResponse signUp(SignUpRequest request) {
        try {
            // 1. Verify Firebase ID token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getFirebaseIdToken());
            String phoneNumber = (String) decodedToken.getClaims().get("phone_number");

            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new ApiException("Firebase token does not contain a phone number", HttpStatus.BAD_REQUEST);
            }

            // 2. Check if phone already registered
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new ApiException("Phone already registered. Please sign in instead.", HttpStatus.CONFLICT);
            }

            // 3. Check if email is already taken (if provided)
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new ApiException("That email address is already registered.", HttpStatus.CONFLICT);
                }
            }

            // 4. Hash password
            String passwordHash = passwordEncoder.encode(request.getPassword());

            // 5. Create and save user
            User user = User.builder()
                    .phoneNumber(phoneNumber)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .passwordHash(passwordHash)
                    .role(User.RoleType.PASSENGER)
                    .isVerified(true) // Firebase-verified phone
                    .isProfileComplete(true)
                    .build();

            user = userRepository.save(user);

            // 6. Generate JWT
            String jwtToken = jwtService.generateToken(user);

            return AuthResponse.builder()
                    .token(jwtToken)
                    .user(buildUserDto(user))
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Sign up failed", e);
            throw new ApiException("Sign up failed. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Normalizes a Bangladesh phone number to the canonical +880 format used by Firebase.
     * Accepts: 01XXXXXXXXX, 8801XXXXXXXXX, +8801XXXXXXXXX
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null) return null;
        phone = phone.trim();
        if (phone.startsWith("+880")) return phone;
        if (phone.startsWith("880")) return "+" + phone;
        if (phone.startsWith("0")) return "+880" + phone.substring(1);
        return phone;
    }

    /**
     * Password-based login. Finds user by phone OR email, validates password, and issues JWT.
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Validate that at least one identifier is provided
        if ((request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) &&
            (request.getEmail() == null || request.getEmail().isBlank())) {
            throw new ApiException("Either phone number or email is required", HttpStatus.BAD_REQUEST);
        }

        // 2. Find user by phone or email
        Optional<User> userOptional;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            // Normalize to +880 format — Firebase stores numbers in international format
            String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
            userOptional = userRepository.findByPhoneNumber(normalizedPhone);
        } else {
            userOptional = userRepository.findByEmail(request.getEmail());
        }

        if (userOptional.isEmpty()) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        User user = userOptional.get();

        // 3. Check if user has a password set (not OTP-only)
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ApiException("This account uses phone OTP. Please use OTP login.", HttpStatus.BAD_REQUEST);
        }

        // 4. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        // 5. Generate JWT
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(buildUserDto(user))
                .build();
    }

    /**
     * Complete profile for OTP-only legacy users.
     * Sets fullName, password, email, and marks profile as complete.
     */
    public AuthResponse completeProfile(String phoneNumber, CompleteProfileRequest request) {
        // 1. Find user
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        // 2. Check if profile is already complete
        if (user.isProfileComplete()) {
            throw new ApiException("Profile is already complete", HttpStatus.BAD_REQUEST);
        }

        // 3. Check if email is taken (if provided)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ApiException("That email address is already registered.", HttpStatus.CONFLICT);
            }
        }

        // 4. Update user
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setProfileComplete(true);

        user = userRepository.save(user);

        // 5. Generate new JWT with updated profile
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(buildUserDto(user))
                .build();
    }

    /**
     * Helper method to build UserDto from User entity.
     */
    private UserDto buildUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .isProfileComplete(user.isProfileComplete())
                .build();
    }
}

package com.shareint.backend.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void configure() {
        try {
            // First try reading from environment variable (useful for production like Railway)
            String base64Credentials = System.getenv("FIREBASE_CREDENTIALS_BASE64");
            InputStream inputStream = null;

            if (base64Credentials != null && !base64Credentials.trim().isEmpty()) {
                log.info("Initializing Firebase Admin SDK from FIREBASE_CREDENTIALS_BASE64 environment variable.");
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Credentials);
                inputStream = new java.io.ByteArrayInputStream(decodedBytes);
            } else {
                // Fallback to local file for development
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                if (resource.exists()) {
                    log.info("Initializing Firebase Admin SDK from classpath resource.");
                    inputStream = resource.getInputStream();
                } else {
                    log.warn("Firebase config not found in env variable or classpath. Firebase Admin SDK will not be initialized.");
                }
            }

            if (inputStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK successfully initialized.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}

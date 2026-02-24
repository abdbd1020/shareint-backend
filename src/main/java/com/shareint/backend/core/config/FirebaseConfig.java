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
            // NOTE: In production, place the 'firebase-service-account.json' inside src/main/resources/ 
            // or provide the path via environment variables rather than hardcoding.
            // For now, this initialization is wrapped in a try-catch to not crash the app if the file is missing during dev.
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            
            if (resource.exists()) {
                InputStream inputStream = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Admin SDK successfully initialized.");
                }
            } else {
                log.warn("Firebase config file 'firebase-service-account.json' not found in classpath. Firebase Admin SDK will not be initialized.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
        }
    }
}

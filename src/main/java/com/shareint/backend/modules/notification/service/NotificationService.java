package com.shareint.backend.modules.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.shareint.backend.modules.user.model.DeviceToken;
import com.shareint.backend.modules.user.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void sendPushNotification(UUID userId, String title, String body) {
        List<DeviceToken> userTokens = deviceTokenRepository.findByUserId(userId);

        if (userTokens.isEmpty()) {
            log.info("No device tokens found for user: {}", userId);
            return;
        }

        for (DeviceToken token : userTokens) {
            Message message = Message.builder()
                    .setToken(token.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent message to token: {}. Firebase Response: {}", token.getFcmToken(), response);
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send push notification to token: {}", token.getFcmToken(), e);
                // Optionally handle unregistered tokens gracefully according to Firebase error codes (e.g. remove them from DB)
            } catch (Exception e) {
                log.error("Error communicating with Firebase Admin SDK", e);
            }
        }
    }
}

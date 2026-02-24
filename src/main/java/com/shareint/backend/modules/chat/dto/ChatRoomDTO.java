package com.shareint.backend.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private UUID id;
    private UUID tripId;
    private UUID passengerId;
    private String passengerName;
    private UUID driverId;
    private String driverName;
    private String firebaseRoomId;
    private Instant createdAt;
}

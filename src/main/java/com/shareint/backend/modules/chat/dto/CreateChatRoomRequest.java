package com.shareint.backend.modules.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomRequest {
    
    @NotNull(message = "Trip ID is required")
    private UUID tripId;
    
    // The driver ID is inferred from the Trip
    // The passenger ID is inferred from the logged-in User
}

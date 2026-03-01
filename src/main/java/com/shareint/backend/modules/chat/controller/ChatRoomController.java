package com.shareint.backend.modules.chat.controller;

import com.shareint.backend.modules.chat.dto.ChatRoomDTO;
import com.shareint.backend.modules.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/web/v1/chats")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * Opens (or returns an existing) chat room for a booking.
     * Works for both the passenger and the driver of that booking.
     */
    @PostMapping("/initiate-by-booking/{bookingId}")
    public ResponseEntity<ChatRoomDTO> initiateChatRoomByBooking(
            Authentication authentication,
            @PathVariable UUID bookingId) {
        String phoneNumber = authentication.getName();
        ChatRoomDTO response = chatRoomService.initiateChatRoomByBooking(phoneNumber, bookingId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

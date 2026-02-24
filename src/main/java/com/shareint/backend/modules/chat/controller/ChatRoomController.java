package com.shareint.backend.modules.chat.controller;

import com.shareint.backend.modules.chat.dto.ChatRoomDTO;
import com.shareint.backend.modules.chat.dto.CreateChatRoomRequest;
import com.shareint.backend.modules.chat.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/initiate")
    public ResponseEntity<ChatRoomDTO> initiateChatRoom(
            Authentication authentication,
            @Valid @RequestBody CreateChatRoomRequest request) {
        String phoneNumber = authentication.getName();
        ChatRoomDTO response = chatRoomService.initiateChatRoom(phoneNumber, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

package com.shareint.backend.modules.booking.controller;

import com.shareint.backend.modules.booking.dto.BookingDTO;
import com.shareint.backend.modules.booking.dto.CreateBookingRequest;
import com.shareint.backend.modules.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(
            Authentication authentication,
            @Valid @RequestBody CreateBookingRequest request) {
        String phoneNumber = authentication.getName();
        BookingDTO response = bookingService.createBooking(phoneNumber, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingDTO>> getMyBookings(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.getMyBookings(phoneNumber));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(
            Authentication authentication,
            @PathVariable java.util.UUID bookingId) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.cancelBooking(phoneNumber, bookingId));
    }
}

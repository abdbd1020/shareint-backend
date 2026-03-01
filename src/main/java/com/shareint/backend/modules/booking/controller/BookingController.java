package com.shareint.backend.modules.booking.controller;

import com.shareint.backend.modules.booking.dto.BookingDTO;
import com.shareint.backend.modules.booking.dto.CreateBookingRequest;
import com.shareint.backend.modules.booking.dto.RecordPaymentRequest;
import com.shareint.backend.modules.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/web/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** Passenger: create a booking (count-based, no seat mapping required) */
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(
            Authentication authentication,
            @Valid @RequestBody CreateBookingRequest request) {
        String phoneNumber = authentication.getName();
        BookingDTO response = bookingService.createBooking(phoneNumber, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Passenger: get their own booking details (includes driver phone) */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBooking(
            Authentication authentication,
            @PathVariable UUID bookingId) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.getBooking(phoneNumber, bookingId));
    }

    /** Passenger: list all their bookings */
    @GetMapping("/me")
    public ResponseEntity<List<BookingDTO>> getMyBookings(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.getMyBookings(phoneNumber));
    }

    /** Passenger: record how they paid the driver (CASH / BKASH / CARD) at end of trip */
    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<BookingDTO> recordPayment(
            Authentication authentication,
            @PathVariable UUID bookingId,
            @Valid @RequestBody RecordPaymentRequest request) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.recordPayment(phoneNumber, bookingId, request));
    }

    /** Passenger: cancel a booking */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(
            Authentication authentication,
            @PathVariable UUID bookingId) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.cancelBooking(phoneNumber, bookingId));
    }

    /** Driver: list all bookings on their trips (to see passengers + commission owed) */
    @GetMapping("/driver")
    public ResponseEntity<List<BookingDTO>> getDriverBookings(Authentication authentication) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.getDriverBookings(phoneNumber));
    }

    /** Driver: mark platform commission as paid to ShareInt for a specific booking */
    @PostMapping("/{bookingId}/commission-paid")
    public ResponseEntity<BookingDTO> markCommissionPaid(
            Authentication authentication,
            @PathVariable UUID bookingId) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(bookingService.markCommissionPaid(phoneNumber, bookingId));
    }
}

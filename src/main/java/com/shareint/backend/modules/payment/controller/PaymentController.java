package com.shareint.backend.modules.payment.controller;

import com.shareint.backend.modules.payment.dto.PaymentDTO;
import com.shareint.backend.modules.payment.dto.PaymentInitiateResponse;
import com.shareint.backend.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/web/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            Authentication authentication,
            @PathVariable UUID bookingId) {
        String phoneNumber = authentication.getName();
        return ResponseEntity.ok(paymentService.initiatePayment(phoneNumber, bookingId));
    }

    // Mock webhook endpoint that SSLCommerz would call server-to-server
    @PostMapping("/webhook/success")
    public ResponseEntity<PaymentDTO> handleSuccessWebhook(@RequestParam("tran_id") String transactionId) {
        // In a real scenario, this endpoint shouldn't be protected by user JWT, 
        // it should be public but validate the SSLCommerz signature.
        // For our mock, we just process exactly.
        return ResponseEntity.ok(paymentService.handleSuccessWebhook(transactionId));
    }
}

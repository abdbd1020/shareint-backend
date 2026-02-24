package com.shareint.backend.modules.payment.dto;

import com.shareint.backend.modules.payment.model.Payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private UUID id;
    private UUID bookingId;
    private UUID passengerId;
    private BigDecimal amount;
    private String gatewayProvider;
    private String transactionId;
    private PaymentStatus status;
    private Instant createdAt;
}

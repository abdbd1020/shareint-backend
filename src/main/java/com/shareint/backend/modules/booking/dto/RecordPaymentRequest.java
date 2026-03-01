package com.shareint.backend.modules.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentRequest {

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "CASH|BKASH|CARD", message = "Payment method must be CASH, BKASH, or CARD")
    private String paymentMethod;
}

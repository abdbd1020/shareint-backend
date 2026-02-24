package com.shareint.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+8801[3-9]\\d{8}$", message = "Must be a valid Bangladesh phone number starting with +880")
    private String phoneNumber;
}

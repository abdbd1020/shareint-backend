package com.shareint.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDriverDocumentsRequest {

    @NotBlank(message = "NID document URL is required")
    private String nidDocumentUrl;

    @NotBlank(message = "Driver's license document URL is required")
    private String driversLicenseUrl;

    @NotBlank(message = "Fitness certificate document URL is required")
    private String fitnessCertificateUrl;
}

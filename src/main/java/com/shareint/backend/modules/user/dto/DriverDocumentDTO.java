package com.shareint.backend.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDocumentDTO {
    private UUID id;
    private String documentType;
    private String status;
    private String documentUrl;

    @JsonProperty("reviewedAt")
    private Instant reviewedAt;

    private String rejectionNote;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;
}

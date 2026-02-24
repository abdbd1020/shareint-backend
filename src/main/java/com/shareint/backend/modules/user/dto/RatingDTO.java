package com.shareint.backend.modules.user.dto;

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
public class RatingDTO {
    private UUID id;
    private UUID tripId;
    private UUID reviewerId;
    private String reviewerName;
    private UUID revieweeId;
    private Integer score;
    private String comment;
    private Instant createdAt;
}

package com.shareint.backend.modules.booking.dto;

import com.shareint.backend.modules.booking.model.Booking.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private UUID id;
    private UUID tripId;
    private UUID passengerId;
    private String passengerName;
    private BigDecimal totalSeatPrice;
    private BigDecimal platformFee;
    private BigDecimal totalCharged;
    private Integer bookedSeatsCount;
    private List<String> seatIdentifiers;
    private BookingStatus status;
    private Instant createdAt;
}

package com.shareint.backend.modules.payment.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.booking.model.Booking;
import com.shareint.backend.modules.booking.repository.BookingRepository;
import com.shareint.backend.modules.payment.dto.PaymentDTO;
import com.shareint.backend.modules.payment.dto.PaymentInitiateResponse;
import com.shareint.backend.modules.payment.model.Payment;
import com.shareint.backend.modules.payment.repository.PaymentRepository;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentInitiateResponse initiatePayment(String passengerPhoneNumber, UUID bookingId) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getPassenger().getId().equals(passenger.getId())) {
            throw new IllegalArgumentException("Booking does not belong to the user");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Booking is not in PENDING_PAYMENT state");
        }

        // Generate a mock transaction ID (in a real app, this might come from SSLCommerz initialization)
        String mockTranId = "TXN-" + System.currentTimeMillis() + "-" + bookingId.toString().substring(0, 8);

        Payment payment = Payment.builder()
                .booking(booking)
                .passenger(passenger)
                .amount(booking.getTotalCharged())
                .gatewayProvider("SSLCOMMERZ_MOCK")
                .transactionId(mockTranId)
                .status(Payment.PaymentStatus.PENDING)
                .creator(passenger)
                .updater(passenger)
                .build();

        paymentRepository.save(payment);

        // Mock redirect URL for the frontend to "complete" the payment
        String mockRedirectUrl = "https://mock-sslcommerz.server/pay?tran_id=" + mockTranId;

        return PaymentInitiateResponse.builder()
                .redirectUrl(mockRedirectUrl)
                .transactionId(mockTranId)
                .build();
    }

    @Transactional
    public PaymentDTO handleSuccessWebhook(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for transaction: " + transactionId));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return mapToDTO(payment); // Already processed
        }

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setGatewayResponse("{\"status\": \"VALID\", \"mocked\": true}");
        payment = paymentRepository.save(payment);

        // Update the booking status to CONFIRMED
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return mapToDTO(payment);
    }

    private PaymentDTO mapToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .passengerId(payment.getPassenger().getId())
                .amount(payment.getAmount())
                .gatewayProvider(payment.getGatewayProvider())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

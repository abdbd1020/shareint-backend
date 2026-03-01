package com.shareint.backend.modules.chat.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.booking.model.Booking;
import com.shareint.backend.modules.booking.repository.BookingRepository;
import com.shareint.backend.modules.chat.dto.ChatRoomDTO;
import com.shareint.backend.modules.chat.model.ChatRoom;
import com.shareint.backend.modules.chat.repository.ChatRoomRepository;
import com.shareint.backend.modules.user.model.User;
import com.shareint.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /**
     * Either the passenger OR the driver can call this with their booking ID.
     * The endpoint verifies that the caller is one of the two parties, then
     * finds or creates the shared chat room.
     */
    @Transactional
    public ChatRoomDTO initiateChatRoomByBooking(String phoneNumber, UUID bookingId) {
        User currentUser = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        User driver = booking.getTrip().getDriver();
        User passenger = booking.getPassenger();

        boolean isDriver = driver.getId().equals(currentUser.getId());
        boolean isPassenger = passenger.getId().equals(currentUser.getId());

        if (!isDriver && !isPassenger) {
            throw new IllegalArgumentException("You are not a participant in this booking");
        }

        return findOrCreateRoom(booking, passenger, driver);
    }

    private ChatRoomDTO findOrCreateRoom(Booking booking, User passenger, User driver) {
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByTripIdAndPassengerIdAndDriverId(
                booking.getTrip().getId(), passenger.getId(), driver.getId());

        if (existingRoom.isPresent()) {
            return mapToDTO(existingRoom.get());
        }

        // Deterministic ID — mobile clients can derive the same value without an API call
        String firebaseRoomId = "room_" + booking.getTrip().getId() + "_" + passenger.getId();

        ChatRoom chatRoom = ChatRoom.builder()
                .trip(booking.getTrip())
                .passenger(passenger)
                .driver(driver)
                .firebaseRoomId(firebaseRoomId)
                .build();

        return mapToDTO(chatRoomRepository.save(chatRoom));
    }

    private ChatRoomDTO mapToDTO(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .tripId(chatRoom.getTrip().getId())
                .passengerId(chatRoom.getPassenger().getId())
                .passengerName(chatRoom.getPassenger().getFullName())
                .driverId(chatRoom.getDriver().getId())
                .driverName(chatRoom.getDriver().getFullName())
                .firebaseRoomId(chatRoom.getFirebaseRoomId())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}

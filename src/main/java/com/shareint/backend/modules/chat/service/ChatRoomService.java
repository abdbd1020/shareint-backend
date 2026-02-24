package com.shareint.backend.modules.chat.service;

import com.shareint.backend.core.exception.ResourceNotFoundException;
import com.shareint.backend.modules.chat.dto.ChatRoomDTO;
import com.shareint.backend.modules.chat.dto.CreateChatRoomRequest;
import com.shareint.backend.modules.chat.model.ChatRoom;
import com.shareint.backend.modules.chat.repository.ChatRoomRepository;
import com.shareint.backend.modules.trip.model.Trip;
import com.shareint.backend.modules.trip.repository.TripRepository;
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
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomDTO initiateChatRoom(String passengerPhoneNumber, CreateChatRoomRequest request) {
        User passenger = userRepository.findByPhoneNumber(passengerPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        User driver = trip.getDriver();

        if (passenger.getId().equals(driver.getId())) {
            throw new IllegalArgumentException("Driver cannot chat with themselves");
        }

        // Ideally, we would also verify if a Booking exists between passenger and trip, 
        // to prevent random users from messaging drivers they haven't booked with.
        
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByTripIdAndPassengerIdAndDriverId(
                trip.getId(), passenger.getId(), driver.getId());

        if (existingRoom.isPresent()) {
            return mapToDTO(existingRoom.get());
        }

        // Create a unique deterministic firebase room ID for easier lookup on the mobile side
        String firebaseRoomId = "room_" + trip.getId() + "_" + passenger.getId();

        ChatRoom chatRoom = ChatRoom.builder()
                .trip(trip)
                .passenger(passenger)
                .driver(driver)
                .firebaseRoomId(firebaseRoomId)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        return mapToDTO(chatRoom);
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

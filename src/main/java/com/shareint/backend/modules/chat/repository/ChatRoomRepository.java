package com.shareint.backend.modules.chat.repository;

import com.shareint.backend.modules.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByTripIdAndPassengerIdAndDriverId(UUID tripId, UUID passengerId, UUID driverId);
}

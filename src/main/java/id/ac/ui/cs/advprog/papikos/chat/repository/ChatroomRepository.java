package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatroomRepository {
    Chatroom save(Chatroom chatroom);
    Optional<Chatroom> findById(UUID chatroomId);
    List<Chatroom> findByRenterId(UUID renterId);
    List<Chatroom> findByOwnerId(UUID ownerId);
    Optional<Chatroom> findByRenterIdAndOwnerIdAndPropertyId(UUID renterId, UUID ownerId, UUID propertyId);
}
package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import java.util.List;
import java.util.Optional;

public interface ChatroomRepository {
    Chatroom save(Chatroom chatroom);
    Optional<Chatroom> findById(Long chatroomId);
    List<Chatroom> findByRenterId(Long renterId);
    List<Chatroom> findByOwnerId(Long ownerId);
    Optional<Chatroom> findByRenterIdAndOwnerIdAndPropertyId(Long renterId, Long ownerId, Long propertyId);
}
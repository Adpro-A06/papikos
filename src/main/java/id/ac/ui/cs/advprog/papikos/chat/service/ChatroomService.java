package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatroomService {

    private final ChatroomRepository chatroomRepository;

    public ChatroomService(ChatroomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }

    public Chatroom createChatroom(Long renterId, Long ownerId, Long propertyId) {
        Optional<Chatroom> existingChatroom = chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);

        if (existingChatroom.isPresent()) {
            return existingChatroom.get();
        }

        Chatroom chatroom = new Chatroom();
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroom.setCreatedAt(LocalDateTime.now());

        return chatroomRepository.save(chatroom);
    }

    public List<Chatroom> getChatroomsByRenterId(Long renterId) {
        return chatroomRepository.findByRenterId(renterId);
    }

    public List<Chatroom> getChatroomsByOwnerId(Long ownerId) {
        return chatroomRepository.findByOwnerId(ownerId);
    }
}

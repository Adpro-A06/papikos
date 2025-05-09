package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatroomServiceImpl implements ChatroomService {
    private final ChatroomRepository chatroomRepository;

    public ChatroomServiceImpl(ChatroomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }

    @Override
    public Chatroom createChatroom(Long renterId, Long ownerId, Long propertyId) {
        Optional<Chatroom> existing = chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);
        if (existing.isPresent()) return existing.get();

        Chatroom c = new Chatroom();
        c.setRenterId(renterId);
        c.setOwnerId(ownerId);
        c.setPropertyId(propertyId);
        c.setCreatedAt(LocalDateTime.now());
        return chatroomRepository.save(c);
    }

    @Override
    public Chatroom getChatroomById(Long id) {
        return chatroomRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Chatroom not found with id: " + id));
    }

    @Override
    public List<Chatroom> getChatroomsByRenterId(Long renterId) {
        return chatroomRepository.findByRenterId(renterId);
    }

    @Override
    public List<Chatroom> getChatroomsByOwnerId(Long ownerId) {
        return chatroomRepository.findByOwnerId(ownerId);
    }
}
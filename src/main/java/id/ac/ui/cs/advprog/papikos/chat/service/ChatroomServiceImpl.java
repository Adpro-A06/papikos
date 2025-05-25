package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.repository.ChatroomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChatroomServiceImpl implements ChatroomService {
    private static final Logger logger = LoggerFactory.getLogger(ChatroomServiceImpl.class);
    private final ChatroomRepository chatroomRepository;

    public ChatroomServiceImpl(ChatroomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }

    @Override
    public Chatroom createChatroom(UUID renterId, UUID ownerId, UUID propertyId) {
        logger.info("=== CREATE CHATROOM DEBUG ===");
        logger.info("Input - renterId: {}, ownerId: {}, propertyId: {}",
                renterId, ownerId, propertyId);

        // Debug: Cek semua chatroom yang ada
        List<Chatroom> allChatrooms = chatroomRepository.findAll();
        logger.info("Total chatrooms in database: {}", allChatrooms.size());

        for (Chatroom c : allChatrooms) {
            logger.info("Existing chatroom: id={}, renterId={}, ownerId={}, propertyId={}",
                    c.getId(), c.getRenterId(), c.getOwnerId(), c.getPropertyId());

            boolean renterMatch = c.getRenterId().equals(renterId);
            boolean ownerMatch = c.getOwnerId().equals(ownerId);
            boolean propertyMatch = c.getPropertyId().equals(propertyId);

            logger.info("Comparison: renterMatch={}, ownerMatch={}, propertyMatch={}",
                    renterMatch, ownerMatch, propertyMatch);

            if (renterMatch && ownerMatch && propertyMatch) {
                logger.info("FOUND EXISTING CHATROOM: {}", c.getId());
                return c;
            }
        }

        logger.info("Trying repository findByRenterIdAndOwnerIdAndPropertyId...");
        Optional<Chatroom> existing = chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);

        if (existing.isPresent()) {
            logger.info("Repository found existing chatroom: {}", existing.get().getId());
            return existing.get();
        } else {
            logger.info("Repository did NOT find existing chatroom");
        }

        logger.info("Creating NEW chatroom...");
        Chatroom chatroom = new Chatroom();
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroom.setCreatedAt(LocalDateTime.now());

        Chatroom savedChatroom = chatroomRepository.save(chatroom);
        logger.info("NEW chatroom created with id: {}", savedChatroom.getId());
        logger.info("=== END CREATE CHATROOM DEBUG ===");

        return savedChatroom;
    }

    @Override
    public Chatroom getChatroomById(UUID id) {
        logger.info("Fetching chatroom with id: {}", id);

        Chatroom chatroom = chatroomRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Chatroom not found with id: {}", id);
                    return new RuntimeException("Chatroom not found with id: " + id);
                });

        logger.info("Chatroom found - id: {}, renterId: {}, ownerId: {}, propertyId: {}",
                chatroom.getId(), chatroom.getRenterId(),
                chatroom.getOwnerId(), chatroom.getPropertyId());

        return chatroom;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chatroom> getChatroomsByRenterId(UUID renterId) {
        logger.info("Fetching chatrooms for renterId: {}", renterId);

        List<Chatroom> chatrooms = chatroomRepository.findByRenterIdForList(renterId);

        logger.info("Found {} chatrooms for renterId: {}", chatrooms.size(), renterId);

        for (Chatroom c : chatrooms) {
            logger.info("Chatroom found: id={}, renterId={}, ownerId={}, propertyId={}",
                    c.getId(), c.getRenterId(), c.getOwnerId(), c.getPropertyId());
        }

        return chatrooms;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chatroom> getChatroomsByOwnerId(UUID ownerId) {
        logger.info("Fetching chatrooms for ownerId: {}", ownerId);

        List<Chatroom> chatrooms = chatroomRepository.findByOwnerIdForList(ownerId);

        logger.info("Found {} chatrooms for ownerId: {}", chatrooms.size(), ownerId);

        return chatrooms;
    }
}
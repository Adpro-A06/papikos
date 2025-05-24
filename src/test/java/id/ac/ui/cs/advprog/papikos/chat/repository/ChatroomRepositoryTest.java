package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatroomRepositoryTest {

    private ChatroomRepository chatroomRepository;

    private UUID renterId;
    private UUID ownerId;
    private UUID propertyId;

    @BeforeEach
    void setUp() {
        chatroomRepository = new ChatroomRepositoryImpl();

        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        Chatroom chatroom = new Chatroom();
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroomRepository.save(chatroom);
    }

    @Test
    void testFindChatroomsByRenterId() {
        List<Chatroom> result = chatroomRepository.findByRenterId(renterId);
        assertEquals(1, result.size());
        assertEquals(renterId, result.getFirst().getRenterId());
    }

    @Test
    void testFindChatroomsByOwnerId() {
        List<Chatroom> result = chatroomRepository.findByOwnerId(ownerId);
        assertEquals(1, result.size());
        assertEquals(ownerId, result.getFirst().getOwnerId());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId() {
        UUID propertyId = chatroomRepository.findByRenterId(renterId).getFirst().getPropertyId();

        Optional<Chatroom> result = chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);

        assertTrue(result.isPresent());
        assertEquals(renterId, result.get().getRenterId());
        assertEquals(ownerId, result.get().getOwnerId());
        assertEquals(propertyId, result.get().getPropertyId());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId_NotFound() {
        Optional<Chatroom> result = chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertTrue(result.isEmpty());
    }
}
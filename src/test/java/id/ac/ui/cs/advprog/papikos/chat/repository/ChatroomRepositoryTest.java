package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatroomRepositoryTest {

    private ChatroomRepository chatroomRepository;

    @BeforeEach
    void setUp() {
        // Langsung instantiate repository di sini
        chatroomRepository = new ChatroomRepositoryImpl();

        // Buat dan simpan chatroom untuk testing
        Chatroom chatroom = new Chatroom();
        chatroom.setRenterId(101L);
        chatroom.setOwnerId(202L);
        chatroom.setPropertyId(303L);
        chatroomRepository.save(chatroom);
    }

    @Test
    void testFindChatroomsByRenterId() {
        List<Chatroom> result = chatroomRepository.findByRenterId(101L);
        assertEquals(1, result.size());
        assertEquals(101L, result.getFirst().getRenterId());
    }

    @Test
    void testFindChatroomsByOwnerId() {
        List<Chatroom> result = chatroomRepository.findByOwnerId(202L);
        assertEquals(1, result.size());
        assertEquals(202L, result.getFirst().getOwnerId());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId() {
        Optional<Chatroom> result = chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(101L, 202L, 303L);

        assertTrue(result.isPresent());
        assertEquals(101L, result.get().getRenterId());
        assertEquals(202L, result.get().getOwnerId());
        assertEquals(303L, result.get().getPropertyId());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId_NotFound() {
        Optional<Chatroom> result = chatroomRepository.findByRenterIdAndOwnerIdAndPropertyId(999L, 888L, 777L);

        assertTrue(result.isEmpty());
    }
}
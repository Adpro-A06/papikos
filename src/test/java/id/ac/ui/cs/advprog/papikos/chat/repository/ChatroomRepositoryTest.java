package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
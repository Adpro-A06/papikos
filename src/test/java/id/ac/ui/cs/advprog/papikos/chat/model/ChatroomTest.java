package id.ac.ui.cs.advprog.papikos.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatroomTest {

    private Chatroom chatroom;
    private LocalDateTime createdAt;

    @BeforeEach
    void setup() {
        createdAt = LocalDateTime.now();
        chatroom = new Chatroom();
        chatroom.setId(1L);
        chatroom.setRenterId(101L);
        chatroom.setOwnerId(202L);
        chatroom.setPropertyId(303L);
        chatroom.setCreatedAt(createdAt);
        chatroom.setMessages(new ArrayList<>());
    }

    @Test
    void testGetId() {
        assertEquals(1L, chatroom.getId());
    }

    @Test
    void testGetRenterId() {
        assertEquals(101L, chatroom.getRenterId());
    }

    @Test
    void testGetOwnerId() {
        assertEquals(202L, chatroom.getOwnerId());
    }

    @Test
    void testGetPropertyId() {
        assertEquals(303L, chatroom.getPropertyId());
    }

    @Test
    void testGetCreatedAt() {
        assertEquals(createdAt, chatroom.getCreatedAt());
    }

    @Test
    void testGetMessages() {
        assertTrue(chatroom.getMessages().isEmpty());
    }

    @Test
    void testAddMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setSenderId(101L);
        message.setChatroomId(1L);
        message.setContent("Test message");
        message.setTimestamp(LocalDateTime.now());

        chatroom.addMessage(message);

        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
    }
}
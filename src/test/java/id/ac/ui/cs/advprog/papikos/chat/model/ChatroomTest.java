package id.ac.ui.cs.advprog.papikos.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChatroomTest {

    private Chatroom chatroom;
    private LocalDateTime createdAt;
    private UUID propertyId;

    @BeforeEach
    void setup() {
        createdAt = LocalDateTime.now();
        chatroom = new Chatroom();
        propertyId = UUID.randomUUID();
        chatroom.setId(1L);
        chatroom.setRenterId(101L);
        chatroom.setOwnerId(202L);
        chatroom.setPropertyId(propertyId);
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
        assertEquals(propertyId, chatroom.getPropertyId());
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
        assertEquals(message, chatroom.getMessages().getFirst());
    }

    @Test
    void testGetLastMessage() {
        Message newMessage = new Message();
        newMessage.setId(2L);
        newMessage.setContent("Newer message");
        newMessage.setTimestamp(LocalDateTime.now().plusMinutes(5));
        chatroom.addMessage(newMessage);

        Message lastMessage = chatroom.getLastMessage();
        assertEquals(newMessage, lastMessage);
    }
}
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
    private UUID chatroomId;
    private UUID renterId;
    private UUID ownerId;

    @BeforeEach
    void setup() {
        createdAt = LocalDateTime.now();
        chatroom = new Chatroom();
        propertyId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();
        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        chatroom.setId(chatroomId);
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroom.setCreatedAt(createdAt);
        chatroom.setMessages(new ArrayList<>());
    }

    @Test
    void testGetId() {
        assertEquals(chatroomId, chatroom.getId());
    }

    @Test
    void testGetRenterId() {
        assertEquals(renterId, chatroom.getRenterId());
    }

    @Test
    void testGetOwnerId() {
        assertEquals(ownerId, chatroom.getOwnerId());
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
        message.setId(UUID.randomUUID());
        message.setSenderId(UUID.randomUUID());
        message.setChatroomId(UUID.randomUUID());
        message.setContent("Test message");
        message.setTimestamp(LocalDateTime.now());

        chatroom.addMessage(message);

        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().getFirst());
    }

    @Test
    void testGetLastMessage() {
        Message newMessage = new Message();
        newMessage.setId(UUID.randomUUID());
        newMessage.setContent("Newer message");
        newMessage.setTimestamp(LocalDateTime.now().plusMinutes(5));
        chatroom.addMessage(newMessage);

        Message lastMessage = chatroom.getLastMessage();
        assertEquals(newMessage, lastMessage);
    }
}
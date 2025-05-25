package id.ac.ui.cs.advprog.papikos.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    private Message message;
    private Chatroom chatroom;
    private LocalDateTime timestamp;
    private UUID messageId;
    private UUID senderId;
    private UUID chatroomId;

    @BeforeEach
    void setup() {
        timestamp = LocalDateTime.now();
        messageId = UUID.randomUUID();
        senderId = UUID.randomUUID();
        chatroomId = UUID.randomUUID();

        chatroom = new Chatroom();
        chatroom.setId(chatroomId);

        message = new Message();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setChatroom(chatroom);
        message.setContent("Hello world!");
        message.setTimestamp(timestamp);
    }

    @Test
    void testGetId() {
        assertEquals(messageId, message.getId());
    }

    @Test
    void testGetSenderId() {
        assertEquals(senderId, message.getSenderId());
    }

    @Test
    void testGetChatroomId() {
        assertEquals(chatroomId, message.getChatroomId());
    }

    @Test
    void testSetChatroomId() {
        UUID newChatroomId = UUID.randomUUID();
        message.setChatroomId(newChatroomId);
        assertEquals(newChatroomId, message.getChatroomId());
    }

    @Test
    void testSetChatroomId_WhenChatroomIsNull() {
        message.setChatroom(null);
        UUID newChatroomId = UUID.randomUUID();
        message.setChatroomId(newChatroomId);
        assertNotNull(message.getChatroom());
        assertEquals(newChatroomId, message.getChatroomId());
    }

    @Test
    void testGetContent() {
        assertEquals("Hello world!", message.getContent());
    }

    @Test
    void testGetTimestamp() {
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void testMessageReadStatus() {
        assertFalse(message.isRead());
        assertNull(message.getReadAt());

        LocalDateTime readTime = LocalDateTime.now();
        message.setRead(true);
        message.setReadAt(readTime);

        assertTrue(message.isRead());
        assertEquals(readTime, message.getReadAt());
    }

    @Test
    void testMessageEditedStatus() {
        assertFalse(message.isEdited());

        message.setEdited(true);
        assertTrue(message.isEdited());
    }

    @Test
    void testMessageDeletedStatus() {
        assertFalse(message.isDeleted());

        message.setDeleted(true);
        assertTrue(message.isDeleted());
    }

    @Test
    void testGetChatroom() {
        assertEquals(chatroom, message.getChatroom());
    }

    @Test
    void testSetChatroom() {
        Chatroom newChatroom = new Chatroom();
        UUID newChatroomId = UUID.randomUUID();
        newChatroom.setId(newChatroomId);

        message.setChatroom(newChatroom);
        assertEquals(newChatroom, message.getChatroom());
        assertEquals(newChatroomId, message.getChatroomId());
    }

    @Test
    void testPrePersist() {
        Message newMessage = new Message();
        assertNull(newMessage.getTimestamp());

        // Call the PrePersist method manually for testing
        newMessage.onCreate();

        assertNotNull(newMessage.getTimestamp());
        assertTrue(newMessage.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(newMessage.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testPrePersist_DoesNotOverrideExistingTimestamp() {
        LocalDateTime existingTime = LocalDateTime.now().minusHours(1);
        message.setTimestamp(existingTime);

        message.onCreate();

        assertEquals(existingTime, message.getTimestamp());
    }

    @Test
    void testDefaultValues() {
        Message newMessage = new Message();
        assertFalse(newMessage.isRead());
        assertFalse(newMessage.isEdited());
        assertFalse(newMessage.isDeleted());
        assertNull(newMessage.getReadAt());
    }
}
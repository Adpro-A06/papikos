package id.ac.ui.cs.advprog.papikos.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    private Message message;
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

        message = new Message();
        message.setId(messageId);
        message.setSenderId(senderId);
        message.setChatroomId(chatroomId);
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
    void testGetContent() {
        assertEquals("Hello world!", message.getContent());
    }

    @Test
    void testGetTimestamp() {
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    void testMessageReadStatus() {
        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
        assertTrue(message.isRead());
        assertNotNull(message.getReadAt());
    }
}

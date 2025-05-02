package id.ac.ui.cs.advprog.papikos.chat.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    private Message message;
    private LocalDateTime timestamp;

    @BeforeEach
    void setup() {
        timestamp = LocalDateTime.now();
        message = new Message();
        message.setId(1L);
        message.setSenderId(101L);
        message.setChatroomId(202L);
        message.setContent("Hello world!");
        message.setTimestamp(timestamp);
    }

    @Test
    void testGetId() {
        assertEquals(1L, message.getId());
    }

    @Test
    void testGetSenderId() {
        assertEquals(101L, message.getSenderId());
    }

    @Test
    void testGetChatroomId() {
        assertEquals(202L, message.getChatroomId());
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

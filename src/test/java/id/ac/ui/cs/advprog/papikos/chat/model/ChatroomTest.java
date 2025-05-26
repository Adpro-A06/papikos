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
        message.setChatroomId(chatroomId);
        message.setContent("Test message");
        message.setTimestamp(LocalDateTime.now());

        chatroom.addMessage(message);

        assertEquals(1, chatroom.getMessages().size());
        assertEquals(message, chatroom.getMessages().get(0));
        assertEquals(chatroom, message.getChatroom());
    }

    @Test
    void testGetLastMessage() {
        Message message1 = new Message();
        message1.setId(UUID.randomUUID());
        message1.setContent("First message");
        message1.setTimestamp(LocalDateTime.now().minusMinutes(10));
        chatroom.addMessage(message1);

        Message message2 = new Message();
        message2.setId(UUID.randomUUID());
        message2.setContent("Second message");
        message2.setTimestamp(LocalDateTime.now().minusMinutes(5));
        chatroom.addMessage(message2);

        Message newMessage = new Message();
        newMessage.setId(UUID.randomUUID());
        newMessage.setContent("Newest message");
        newMessage.setTimestamp(LocalDateTime.now());
        chatroom.addMessage(newMessage);

        Message lastMessage = chatroom.getLastMessage();
        assertEquals(newMessage, lastMessage);
    }

    @Test
    void testGetLastMessage_EmptyMessages() {
        chatroom.setMessages(new ArrayList<>());
        Message lastMessage = chatroom.getLastMessage();
        assertNull(lastMessage);
    }

    @Test
    void testGetUnreadMessageCount() {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Message ownMessage = new Message();
        ownMessage.setId(UUID.randomUUID());
        ownMessage.setSenderId(currentUserId);
        ownMessage.setRead(false);
        chatroom.addMessage(ownMessage);

        Message unreadMessage1 = new Message();
        unreadMessage1.setId(UUID.randomUUID());
        unreadMessage1.setSenderId(otherUserId);
        unreadMessage1.setRead(false);
        chatroom.addMessage(unreadMessage1);

        Message unreadMessage2 = new Message();
        unreadMessage2.setId(UUID.randomUUID());
        unreadMessage2.setSenderId(otherUserId);
        unreadMessage2.setRead(false);
        chatroom.addMessage(unreadMessage2);

        Message readMessage = new Message();
        readMessage.setId(UUID.randomUUID());
        readMessage.setSenderId(otherUserId);
        readMessage.setRead(true);
        chatroom.addMessage(readMessage);

        int unreadCount = chatroom.getUnreadMessageCount(currentUserId);
        assertEquals(2, unreadCount);
    }

    @Test
    void testPrePersist() {
        Chatroom newChatroom = new Chatroom();
        assertNull(newChatroom.getCreatedAt());
        newChatroom.onCreate();

        assertNotNull(newChatroom.getCreatedAt());
        assertTrue(newChatroom.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(newChatroom.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testPrePersist_DoesNotOverrideExistingCreatedAt() {
        LocalDateTime existingTime = LocalDateTime.now().minusHours(1);
        chatroom.setCreatedAt(existingTime);

        chatroom.onCreate();

        assertEquals(existingTime, chatroom.getCreatedAt());
    }
}
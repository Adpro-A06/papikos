package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private UUID chatroomId;
    private UUID senderId;
    private UUID otherUserId;
    private Chatroom chatroom;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        // Create and persist chatroom
        chatroom = new Chatroom();
        chatroom.setRenterId(senderId);
        chatroom.setOwnerId(otherUserId);
        chatroom.setPropertyId(UUID.randomUUID());
        chatroom.setCreatedAt(LocalDateTime.now());
        chatroom = entityManager.persistAndFlush(chatroom);

        // Create and persist messages with explicit read status
        Message message1 = new Message();
        message1.setSenderId(senderId);
        message1.setContent("Hello!");
        message1.setTimestamp(LocalDateTime.of(2023, 1, 1, 10, 0));
        message1.setRead(true); // Explicitly set as read
        chatroom.addMessage(message1);
        entityManager.persistAndFlush(message1);

        Message message2 = new Message();
        message2.setSenderId(otherUserId);
        message2.setContent("Hi there!");
        message2.setTimestamp(LocalDateTime.of(2023, 1, 1, 11, 0));
        message2.setRead(true); // Explicitly set as read
        chatroom.addMessage(message2);
        entityManager.persistAndFlush(message2);

        entityManager.clear();
    }

    @Test
    void testFindByChatroomId() {
        List<Message> result = messageRepository.findByChatroomId(chatroom.getId());

        assertEquals(2, result.size());
        // Should be ordered by timestamp ASC
        assertEquals("Hello!", result.get(0).getContent());
        assertEquals("Hi there!", result.get(1).getContent());
    }

    @Test
    void testFindByChatroomIdOrderByTimestampDesc() {
        List<Message> result = messageRepository.findByChatroomIdOrderByTimestampDesc(chatroom.getId());

        assertEquals(2, result.size());
        // Should be ordered by timestamp DESC
        assertEquals("Hi there!", result.get(0).getContent());
        assertEquals("Hello!", result.get(1).getContent());
    }

    @Test
    void testFindActiveByChatroomId() {
        // Add a deleted message
        Message deletedMessage = new Message();
        deletedMessage.setSenderId(senderId);
        deletedMessage.setContent("Deleted message");
        deletedMessage.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));
        deletedMessage.setDeleted(true);
        deletedMessage.setRead(true);
        chatroom.addMessage(deletedMessage);
        entityManager.persistAndFlush(deletedMessage);
        entityManager.clear();

        List<Message> result = messageRepository.findActiveByChatroomId(chatroom.getId());

        assertEquals(2, result.size()); // Should not include deleted message
        assertEquals("Hello!", result.get(0).getContent());
        assertEquals("Hi there!", result.get(1).getContent());
    }

    @Test
    void testCountUnreadMessagesByChatroomIdAndUserId() {
        // First, clean up existing messages to start fresh
        entityManager.clear();

        // Create unread messages from other user
        Message unreadMessage1 = new Message();
        unreadMessage1.setSenderId(otherUserId);
        unreadMessage1.setContent("Unread 1");
        unreadMessage1.setTimestamp(LocalDateTime.now());
        unreadMessage1.setRead(false); // Explicitly unread
        chatroom.addMessage(unreadMessage1);
        entityManager.persistAndFlush(unreadMessage1);

        Message unreadMessage2 = new Message();
        unreadMessage2.setSenderId(otherUserId);
        unreadMessage2.setContent("Unread 2");
        unreadMessage2.setTimestamp(LocalDateTime.now().plusMinutes(1));
        unreadMessage2.setRead(false); // Explicitly unread
        chatroom.addMessage(unreadMessage2);
        entityManager.persistAndFlush(unreadMessage2);

        // Create read message from other user
        Message readMessage = new Message();
        readMessage.setSenderId(otherUserId);
        readMessage.setContent("Read message");
        readMessage.setTimestamp(LocalDateTime.now().plusMinutes(2));
        readMessage.setRead(true); // Explicitly read
        chatroom.addMessage(readMessage);
        entityManager.persistAndFlush(readMessage);

        // Create message from current user (should not count)
        Message ownMessage = new Message();
        ownMessage.setSenderId(senderId);
        ownMessage.setContent("Own message");
        ownMessage.setTimestamp(LocalDateTime.now().plusMinutes(3));
        ownMessage.setRead(false); // Even if unread, shouldn't count since it's from senderId
        chatroom.addMessage(ownMessage);
        entityManager.persistAndFlush(ownMessage);

        entityManager.clear();

        // Debug: Check total messages
        List<Message> allMessages = messageRepository.findByChatroomId(chatroom.getId());
        System.out.println("Total messages in chatroom: " + allMessages.size());
        for (Message msg : allMessages) {
            System.out.println("Message: " + msg.getContent() +
                    ", SenderId: " + msg.getSenderId() +
                    ", Read: " + msg.isRead() +
                    ", SenderId equals current user: " + msg.getSenderId().equals(senderId));
        }

        int unreadCount = messageRepository.countUnreadMessagesByChatroomIdAndUserId(
                chatroom.getId(), senderId);

        System.out.println("Unread count result: " + unreadCount);
        assertEquals(2, unreadCount); // Only unread messages from other users
    }

    @Test
    void testFindByChatroomId_EmptyResult() {
        UUID nonExistentChatroomId = UUID.randomUUID();
        List<Message> result = messageRepository.findByChatroomId(nonExistentChatroomId);

        assertEquals(0, result.size());
    }

    @Test
    void testCountUnreadMessages_NoUnreadMessages() {
        // Set all existing messages as read
        List<Message> messages = messageRepository.findByChatroomId(chatroom.getId());
        for (Message message : messages) {
            message.setRead(true);
            entityManager.merge(message);
        }
        entityManager.flush();

        int unreadCount = messageRepository.countUnreadMessagesByChatroomIdAndUserId(
                chatroom.getId(), senderId);

        assertEquals(0, unreadCount);
    }

    @Test
    void testCountUnreadMessages_OnlyFromOtherUsers() {
        // Create a fresh chatroom for this test
        Chatroom testChatroom = new Chatroom();
        testChatroom.setRenterId(senderId);
        testChatroom.setOwnerId(otherUserId);
        testChatroom.setPropertyId(UUID.randomUUID());
        testChatroom.setCreatedAt(LocalDateTime.now());
        testChatroom = entityManager.persistAndFlush(testChatroom);

        // Add unread message from other user - should count
        Message unreadFromOther = new Message();
        unreadFromOther.setSenderId(otherUserId);
        unreadFromOther.setContent("Unread from other");
        unreadFromOther.setTimestamp(LocalDateTime.now());
        unreadFromOther.setRead(false);
        testChatroom.addMessage(unreadFromOther);
        entityManager.persistAndFlush(unreadFromOther);

        // Add unread message from current user - should NOT count
        Message unreadFromSelf = new Message();
        unreadFromSelf.setSenderId(senderId);
        unreadFromSelf.setContent("Unread from self");
        unreadFromSelf.setTimestamp(LocalDateTime.now());
        unreadFromSelf.setRead(false);
        testChatroom.addMessage(unreadFromSelf);
        entityManager.persistAndFlush(unreadFromSelf);

        entityManager.clear();

        int unreadCount = messageRepository.countUnreadMessagesByChatroomIdAndUserId(
                testChatroom.getId(), senderId);

        assertEquals(1, unreadCount); // Only the message from other user
    }

    @Test
    void testCountUnreadMessages_OnlyUnreadMessages() {
        // Create a fresh chatroom for this test
        Chatroom testChatroom = new Chatroom();
        testChatroom.setRenterId(senderId);
        testChatroom.setOwnerId(otherUserId);
        testChatroom.setPropertyId(UUID.randomUUID());
        testChatroom.setCreatedAt(LocalDateTime.now());
        testChatroom = entityManager.persistAndFlush(testChatroom);

        // Add read message from other user - should NOT count
        Message readFromOther = new Message();
        readFromOther.setSenderId(otherUserId);
        readFromOther.setContent("Read from other");
        readFromOther.setTimestamp(LocalDateTime.now());
        readFromOther.setRead(true);
        testChatroom.addMessage(readFromOther);
        entityManager.persistAndFlush(readFromOther);

        // Add unread message from other user - should count
        Message unreadFromOther = new Message();
        unreadFromOther.setSenderId(otherUserId);
        unreadFromOther.setContent("Unread from other");
        unreadFromOther.setTimestamp(LocalDateTime.now());
        unreadFromOther.setRead(false);
        testChatroom.addMessage(unreadFromOther);
        entityManager.persistAndFlush(unreadFromOther);

        entityManager.clear();

        int unreadCount = messageRepository.countUnreadMessagesByChatroomIdAndUserId(
                testChatroom.getId(), senderId);

        assertEquals(1, unreadCount); // Only the unread message from other user
    }

    @Test
    void testFindByChatroomIdOrderByTimestampDesc_WithMultipleMessages() {
        // Add more messages with different timestamps
        Message message3 = new Message();
        message3.setSenderId(senderId);
        message3.setContent("Latest message");
        message3.setTimestamp(LocalDateTime.of(2023, 1, 1, 12, 0));
        message3.setRead(true);
        chatroom.addMessage(message3);
        entityManager.persistAndFlush(message3);

        entityManager.clear();

        List<Message> result = messageRepository.findByChatroomIdOrderByTimestampDesc(chatroom.getId());

        assertEquals(3, result.size());
        // Should be ordered by timestamp DESC (latest first)
        assertEquals("Latest message", result.get(0).getContent());
        assertEquals("Hi there!", result.get(1).getContent());
        assertEquals("Hello!", result.get(2).getContent());
    }
}
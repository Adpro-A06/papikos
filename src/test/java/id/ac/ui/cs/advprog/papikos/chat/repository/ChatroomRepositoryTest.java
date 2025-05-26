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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ChatroomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatroomRepository chatroomRepository;

    private UUID renterId;
    private UUID ownerId;
    private UUID propertyId;
    private Chatroom savedChatroom;

    @BeforeEach
    void setUp() {
        renterId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        propertyId = UUID.randomUUID();

        Chatroom chatroom = new Chatroom();
        chatroom.setRenterId(renterId);
        chatroom.setOwnerId(ownerId);
        chatroom.setPropertyId(propertyId);
        chatroom.setCreatedAt(LocalDateTime.now());

        savedChatroom = entityManager.persistAndFlush(chatroom);
        entityManager.clear();
    }

    @Test
    void testFindByRenterId() {
        List<Chatroom> result = chatroomRepository.findByRenterId(renterId);

        assertEquals(1, result.size());
        assertEquals(renterId, result.get(0).getRenterId());
        assertEquals(ownerId, result.get(0).getOwnerId());
        assertEquals(propertyId, result.get(0).getPropertyId());
    }

    @Test
    void testFindByOwnerId() {
        List<Chatroom> result = chatroomRepository.findByOwnerId(ownerId);

        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
        assertEquals(renterId, result.get(0).getRenterId());
        assertEquals(propertyId, result.get(0).getPropertyId());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId() {
        Optional<Chatroom> result = chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);

        assertTrue(result.isPresent());
        assertEquals(renterId, result.get().getRenterId());
        assertEquals(ownerId, result.get().getOwnerId());
        assertEquals(propertyId, result.get().getPropertyId());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId_NotFound() {
        Optional<Chatroom> result = chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByIdWithMessages() {
        Message message1 = new Message();
        message1.setSenderId(renterId);
        message1.setContent("First message");
        message1.setTimestamp(LocalDateTime.now().minusMinutes(5));

        savedChatroom.addMessage(message1);
        entityManager.persistAndFlush(message1);

        Message message2 = new Message();
        message2.setSenderId(ownerId);
        message2.setContent("Second message");
        message2.setTimestamp(LocalDateTime.now());
   
        savedChatroom.addMessage(message2);
        entityManager.persistAndFlush(message2);

        entityManager.clear();

        Optional<Chatroom> result = chatroomRepository.findByIdWithMessages(savedChatroom.getId());

        assertTrue(result.isPresent());
        Chatroom chatroom = result.get();
        assertEquals(2, chatroom.getMessages().size());

        List<Message> messages = chatroom.getMessages();
        assertEquals("First message", messages.get(0).getContent());
        assertEquals("Second message", messages.get(1).getContent());
    }

    @Test
    void testFindByRenterIdWithMessages() {
        Message message = new Message();
        message.setSenderId(renterId);
        message.setContent("Test message");
        message.setTimestamp(LocalDateTime.now());

        savedChatroom.addMessage(message);
        entityManager.persistAndFlush(message);
        entityManager.clear();

        List<Chatroom> result = chatroomRepository.findByRenterIdWithMessages(renterId);

        assertEquals(1, result.size());
        Chatroom chatroom = result.get(0);
        assertEquals(renterId, chatroom.getRenterId());
        assertNotNull(chatroom.getMessages());
        assertEquals(1, chatroom.getMessages().size());
        assertEquals("Test message", chatroom.getMessages().get(0).getContent());
    }

    @Test
    void testFindByOwnerIdWithMessages() {
        Message message = new Message();
        message.setSenderId(ownerId);
        message.setContent("Owner message");
        message.setTimestamp(LocalDateTime.now());

        savedChatroom.addMessage(message);
        entityManager.persistAndFlush(message);
        entityManager.clear();

        List<Chatroom> result = chatroomRepository.findByOwnerIdWithMessages(ownerId);

        assertEquals(1, result.size());
        Chatroom chatroom = result.get(0);
        assertEquals(ownerId, chatroom.getOwnerId());
        assertNotNull(chatroom.getMessages());
        assertEquals(1, chatroom.getMessages().size());
        assertEquals("Owner message", chatroom.getMessages().get(0).getContent());
    }

    @Test
    void testFindByRenterIdForList() {
        List<Chatroom> result = chatroomRepository.findByRenterIdForList(renterId);

        assertEquals(1, result.size());
        assertEquals(renterId, result.get(0).getRenterId());
    }

    @Test
    void testFindByOwnerIdForList() {
        List<Chatroom> result = chatroomRepository.findByOwnerIdForList(ownerId);

        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());
    }

    @Test
    void testOrderingByCreatedAtDesc() {
        Chatroom olderChatroom = new Chatroom();
        olderChatroom.setRenterId(renterId);
        olderChatroom.setOwnerId(UUID.randomUUID());
        olderChatroom.setPropertyId(UUID.randomUUID());
        olderChatroom.setCreatedAt(LocalDateTime.now().minusHours(1));
        entityManager.persistAndFlush(olderChatroom);
        entityManager.clear();

        List<Chatroom> result = chatroomRepository.findByRenterId(renterId);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
    }

    @Test
    void testFindByIdWithMessages_EmptyMessages() {
        Optional<Chatroom> result = chatroomRepository.findByIdWithMessages(savedChatroom.getId());

        assertTrue(result.isPresent());
        Chatroom chatroom = result.get();
        assertNotNull(chatroom.getMessages());
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testFindByRenterIdWithMessages_NoMessages() {
        entityManager.clear();

        List<Chatroom> result = chatroomRepository.findByRenterIdWithMessages(renterId);

        assertEquals(1, result.size());
        Chatroom chatroom = result.get(0);
        assertEquals(renterId, chatroom.getRenterId());
        assertNotNull(chatroom.getMessages());
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testFindByOwnerIdWithMessages_NoMessages() {
        entityManager.clear();

        List<Chatroom> result = chatroomRepository.findByOwnerIdWithMessages(ownerId);

        assertEquals(1, result.size());
        Chatroom chatroom = result.get(0);
        assertEquals(ownerId, chatroom.getOwnerId());
        assertNotNull(chatroom.getMessages());
        assertEquals(0, chatroom.getMessages().size());
    }

    @Test
    void testFindByRenterIdAndOwnerIdAndPropertyId_WithMultipleChatrooms() {
        Chatroom chatroom2 = new Chatroom();
        chatroom2.setRenterId(renterId);
        chatroom2.setOwnerId(UUID.randomUUID());
        chatroom2.setPropertyId(UUID.randomUUID());
        chatroom2.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(chatroom2);

        Chatroom chatroom3 = new Chatroom();
        chatroom3.setRenterId(UUID.randomUUID());
        chatroom3.setOwnerId(ownerId);
        chatroom3.setPropertyId(UUID.randomUUID());
        chatroom3.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(chatroom3);

        entityManager.clear();
        Optional<Chatroom> result = chatroomRepository
                .findByRenterIdAndOwnerIdAndPropertyId(renterId, ownerId, propertyId);

        assertTrue(result.isPresent());
        assertEquals(savedChatroom.getId(), result.get().getId());
    }
}
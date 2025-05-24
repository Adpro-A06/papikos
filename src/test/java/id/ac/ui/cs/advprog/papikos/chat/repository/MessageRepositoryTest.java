package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageRepositoryTest {

    private MessageRepositoryImpl messageRepository;

    private UUID chatroomId;
    private UUID senderId;

    @BeforeEach
    void setUp() {
        messageRepository = new MessageRepositoryImpl();
        chatroomId = UUID.randomUUID();
        senderId = UUID.randomUUID();

        Message message = new Message();
        message.setChatroomId(chatroomId);
        message.setSenderId(senderId);
        message.setContent("Hello!");
        message.setTimestamp(LocalDateTime.of(2023, 1, 1, 10, 0));

        messageRepository.save(message);
    }

    @Test
    void testFindMessagesByChatroomId() {
        List<Message> result = messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId);
        assertEquals(1, result.size());
        assertEquals("Hello!", result.getFirst().getContent());
        assertEquals(senderId, result.getFirst().getSenderId());
    }
}

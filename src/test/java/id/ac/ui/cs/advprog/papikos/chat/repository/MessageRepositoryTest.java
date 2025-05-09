package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageRepositoryTest {

    private MessageRepositoryImpl messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository = new MessageRepositoryImpl();

        Message message = new Message();
        message.setChatroomId(1L);
        message.setSenderId(101L);
        message.setContent("Hello!");
        message.setTimestamp(LocalDateTime.of(2023, 1, 1, 10, 0));

        messageRepository.save(message);
    }

    @Test
    void testFindMessagesByChatroomId() {
        List<Message> result = messageRepository.findByChatroomIdOrderByTimestampDesc(1L);
        assertEquals(1, result.size());
        assertEquals("Hello!", result.getFirst().getContent());
        assertEquals(101L, result.getFirst().getSenderId());
    }
}

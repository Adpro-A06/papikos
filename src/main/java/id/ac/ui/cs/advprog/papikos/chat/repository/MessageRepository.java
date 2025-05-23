package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(UUID id);
    List<Message> findByChatroomId(UUID chatroomId);
    List<Message> findByChatroomIdOrderByTimestampDesc(UUID chatroomId);
    void deleteById(UUID id);
}

package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(Long id);
    List<Message> findByChatroomId(Long chatroomId);
    List<Message> findByChatroomIdOrderByTimestampDesc(Long chatroomId);
    void deleteById(Long id);
}

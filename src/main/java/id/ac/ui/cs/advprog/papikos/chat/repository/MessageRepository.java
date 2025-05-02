package id.ac.ui.cs.advprog.papikos.chat.repository;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import java.util.List;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findByChatroomIdOrderByTimestampAsc(Long chatroomId);
}

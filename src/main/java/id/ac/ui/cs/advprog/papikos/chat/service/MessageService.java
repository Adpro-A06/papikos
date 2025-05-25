package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    List<Message> getMessagesByChatroomId(UUID chatroomId);  // Changed from Long to UUID
    Message sendMessage(UUID chatroomId, UUID senderId, String content);  // Changed from Long to UUID
    Message editMessage(UUID chatroomId, UUID messageId, String newContent);  // Changed from Long to UUID
    boolean deleteMessage(UUID chatroomId, UUID messageId);  // Changed from Long to UUID
    boolean undoLastAction(UUID chatroomId, UUID messageId);  // Changed from Long to UUID
}

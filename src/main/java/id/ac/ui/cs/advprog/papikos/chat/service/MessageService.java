package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    List<Message> getMessagesByChatroomId(UUID chatroomId);
    Message sendMessage(UUID chatroomId, UUID senderId, String content);
    Message editMessage(UUID chatroomId, UUID messageId, String newContent);
    boolean deleteMessage(UUID chatroomId, UUID messageId);
    boolean undoLastAction(UUID chatroomId, UUID messageId);
}

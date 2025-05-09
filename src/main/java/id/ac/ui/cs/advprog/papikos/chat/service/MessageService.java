package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import java.util.List;

public interface MessageService {
    Message sendMessage(Long chatroomId, Long senderId, String content);
    Message editMessage(Long chatroomId, Long messageId, String newContent);
    boolean deleteMessage(Long chatroomId, Long messageId);
    boolean undoLastAction(Long chatroomId, Long messageId);
}
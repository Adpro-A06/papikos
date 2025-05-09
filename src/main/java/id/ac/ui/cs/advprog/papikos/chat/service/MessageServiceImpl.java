package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    private final ChatroomService  chatroomService;
    private final ChatCommandService chatCommandService;

    public MessageServiceImpl(ChatroomService chatroomService, ChatCommandService chatCommandService) {
        this.chatroomService    = chatroomService;
        this.chatCommandService = chatCommandService;
    }

    @Override
    public Message sendMessage(Long chatroomId, Long senderId, String content) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        return chatCommandService.sendMessage(c, senderId, content);
    }

    @Override
    public Message editMessage(Long chatroomId, Long messageId, String newContent) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        return chatCommandService.editMessage(c, messageId, newContent);
    }

    @Override
    public boolean deleteMessage(Long chatroomId, Long messageId) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        return chatCommandService.deleteMessage(c, messageId);
    }

    @Override
    public boolean undoLastAction(Long chatroomId, Long messageId) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        return chatCommandService.undoLastCommand(c, messageId);
    }
}
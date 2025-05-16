package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final ChatroomServiceImpl  chatroomService;
    private final ChatCommandService chatCommandService;
    private final MessageRepository messageRepository;

    public MessageServiceImpl(ChatroomServiceImpl chatroomService, ChatCommandService chatCommandService, MessageRepository messageRepository) {
        this.chatroomService    = chatroomService;
        this.chatCommandService = chatCommandService;
        this.messageRepository  = messageRepository;
    }

    @Override
    public List<Message> getMessagesByChatroomId(Long chatroomId) {
        return messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId);
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
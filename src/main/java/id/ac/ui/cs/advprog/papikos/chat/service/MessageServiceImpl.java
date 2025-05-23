package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {
    private final ChatroomServiceImpl chatroomService;
    private final ChatCommandService chatCommandService;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(ChatroomServiceImpl chatroomService,
                              ChatCommandService chatCommandService,
                              MessageRepository messageRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.chatroomService = chatroomService;
        this.chatCommandService = chatCommandService;
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public List<Message> getMessagesByChatroomId(UUID chatroomId) {
        return messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId);
    }

    @Override
    public Message sendMessage(UUID chatroomId, UUID senderId, String content) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        Message message = chatCommandService.sendMessage(c, senderId, content);

        // Broadcast message melalui WebSocket
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId,
                message
        );

        return message;
    }

    @Override
    public Message editMessage(UUID chatroomId, UUID messageId, String newContent) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        Message message = chatCommandService.editMessage(c, messageId, newContent);

        // Broadcast message edit melalui WebSocket
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId + "/edit",
                message
        );

        return message;
    }

    @Override
    public boolean deleteMessage(UUID chatroomId, UUID messageId) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        boolean success = chatCommandService.deleteMessage(c, messageId);

        if (success) {
            // Broadcast message delete melalui WebSocket
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatroomId + "/delete",
                    messageId
            );
        }

        return success;
    }

    @Override
    public boolean undoLastAction(UUID chatroomId, UUID messageId) {
        Chatroom c = chatroomService.getChatroomById(chatroomId);
        boolean success = chatCommandService.undoLastCommand(c, messageId);

        if (success) {
            // Broadcast undo action melalui WebSocket - reload semua messages
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatroomId + "/reload",
                    "Messages reloaded due to undo"
            );
        }

        return success;
    }
}
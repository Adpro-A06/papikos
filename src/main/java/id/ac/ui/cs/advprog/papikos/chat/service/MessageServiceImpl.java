package id.ac.ui.cs.advprog.papikos.chat.service;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

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
    @Transactional(readOnly = true)
    public List<Message> getMessagesByChatroomId(UUID chatroomId) {
        logger.info("Fetching messages for chatroomId: {}", chatroomId);

        List<Message> messages = messageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId);

        logger.info("Found {} messages for chatroomId: {}", messages.size(), chatroomId);

        for (int i = 0; i < Math.min(3, messages.size()); i++) {
            Message m = messages.get(i);
            logger.info("Message[{}]: id={}, senderId={}, content='{}', timestamp={}",
                    i, m.getId(), m.getSenderId(),
                    m.getContent().substring(0, Math.min(50, m.getContent().length())),
                    m.getTimestamp());
        }

        return messages;
    }

    @Override
    public Message sendMessage(UUID chatroomId, UUID senderId, String content) {
        logger.info("Sending message - chatroomId: {}, senderId: {}, content length: {}",
                chatroomId, senderId, content.length());

        Chatroom chatroom = chatroomService.getChatroomById(chatroomId);

        Message message = chatCommandService.sendMessage(chatroom, senderId, content);

        logger.info("Message sent successfully - messageId: {}, chatroomId: {}",
                message.getId(), chatroomId);

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId,
                message
        );

        return message;
    }

    @Override
    public Message editMessage(UUID chatroomId, UUID messageId, String newContent) {
        logger.info("Editing message - chatroomId: {}, messageId: {}", chatroomId, messageId);

        Chatroom chatroom = chatroomService.getChatroomById(chatroomId);
        Message message = chatCommandService.editMessage(chatroom, messageId, newContent);

        logger.info("Message edited successfully - messageId: {}", messageId);

        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId + "/edit",
                message
        );

        return message;
    }

    @Override
    public boolean deleteMessage(UUID chatroomId, UUID messageId) {
        logger.info("Deleting message - chatroomId: {}, messageId: {}", chatroomId, messageId);

        Chatroom chatroom = chatroomService.getChatroomById(chatroomId);
        boolean success = chatCommandService.deleteMessage(chatroom, messageId);

        if (success) {
            logger.info("Message deleted successfully - messageId: {}", messageId);

            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatroomId + "/delete",
                    messageId
            );
        } else {
            logger.warn("Failed to delete message - messageId: {}", messageId);
        }

        return success;
    }

    @Override
    public boolean undoLastAction(UUID chatroomId, UUID messageId) {
        logger.info("Undoing last action - chatroomId: {}, messageId: {}", chatroomId, messageId);

        Chatroom chatroom = chatroomService.getChatroomById(chatroomId);
        boolean success = chatCommandService.undoLastCommand(chatroom, messageId);

        if (success) {
            logger.info("Undo successful - chatroomId: {}", chatroomId);

            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatroomId + "/reload",
                    "Messages reloaded due to undo"
            );
        } else {
            logger.warn("Undo failed - chatroomId: {}", chatroomId);
        }

        return success;
    }
}
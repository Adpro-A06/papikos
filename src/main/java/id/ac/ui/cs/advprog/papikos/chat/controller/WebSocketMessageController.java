package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketMessageController {

    private final MessageServiceImpl messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageController.class);

    public WebSocketMessageController(MessageServiceImpl messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{chatroomId}/send")
    public void sendMessage(@DestinationVariable UUID chatroomId, ChatMessage chatMessage) {
        try {
            logger.info("Received WebSocket message for chatroom: {}", chatroomId);

            Message savedMessage = messageService.sendMessage(
                    chatroomId,
                    chatMessage.getSenderId(),
                    chatMessage.getContent()
            );

            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatroomId,
                    savedMessage
            );

            logger.info("Message broadcasted to chatroom: {}", chatroomId);
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: ", e);
        }
    }

    public void broadcastMessageEdit(UUID chatroomId, Message message) {
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId + "/edit",
                message
        );
    }

    public void broadcastMessageDelete(UUID chatroomId, UUID messageId) {
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId + "/delete",
                messageId
        );
    }

    public static class ChatMessage {
        private UUID senderId;
        private String content;

        public ChatMessage() {}

        public UUID getSenderId() {
            return senderId;
        }

        public void setSenderId(UUID senderId) {
            this.senderId = senderId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
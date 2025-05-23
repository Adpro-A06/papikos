package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageService;
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

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageController.class);

    public WebSocketMessageController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{chatroomId}/send")
    public void sendMessage(@DestinationVariable UUID chatroomId, ChatMessage chatMessage) {
        try {
            logger.info("Received WebSocket message for chatroom: {}", chatroomId);

            // Save message menggunakan existing service
            Message savedMessage = messageService.sendMessage(
                    chatroomId,
                    chatMessage.getSenderId(),
                    chatMessage.getContent()
            );

            // Broadcast message ke semua subscriber di chatroom ini
            messagingTemplate.convertAndSend(
                    "/topic/chatroom/" + chatroomId,
                    savedMessage
            );

            logger.info("Message broadcasted to chatroom: {}", chatroomId);
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: ", e);
        }
    }

    // Method untuk broadcast message edit
    public void broadcastMessageEdit(UUID chatroomId, Message message) {
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId + "/edit",
                message
        );
    }

    // Method untuk broadcast message delete
    public void broadcastMessageDelete(UUID chatroomId, UUID messageId) {
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroomId + "/delete",
                messageId
        );
    }

    // DTO class untuk WebSocket message
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
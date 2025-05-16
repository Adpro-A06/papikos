package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(MessageService messageService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chatrooms/{chatroomId}/send")
    public void sendMessage(@DestinationVariable Long chatroomId, ChatMessageRequest request) {
        Message message = messageService.sendMessage(
                chatroomId,
                request.getSenderId(),
                request.getContent()
        );
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId, message);
    }

    @MessageMapping("/chatrooms/{chatroomId}/edit/{messageId}")
    public void editMessage(
            @DestinationVariable Long chatroomId,
            @DestinationVariable Long messageId,
            ChatMessageRequest request) {
        Message message = messageService.editMessage(
                chatroomId,
                messageId,
                request.getContent()
        );
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId, message);
    }

    @MessageMapping("/chatrooms/{chatroomId}/delete/{messageId}")
    public void deleteMessage(
            @DestinationVariable Long chatroomId,
            @DestinationVariable Long messageId) {
        boolean success = messageService.deleteMessage(chatroomId, messageId);
        if (success) {
            messagingTemplate.convertAndSend("/topic/chatrooms/" + chatroomId + "/delete", messageId);
        }
    }

    public static class ChatMessageRequest {
        private Long senderId;
        private String content;

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
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
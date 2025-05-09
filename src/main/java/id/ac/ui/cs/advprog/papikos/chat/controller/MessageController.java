package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Message;
import id.ac.ui.cs.advprog.papikos.chat.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chatrooms/{chatroomId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<Message>> getMessagesByChatroomId(@PathVariable Long chatroomId) {
        List<Message> messages = messageService.getMessagesByChatroomId(chatroomId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(
            @PathVariable Long chatroomId,
            @RequestBody MessageRequest request) {
        Message message = messageService.sendMessage(
                chatroomId,
                request.getSenderId(),
                request.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Message> editMessage(
            @PathVariable Long chatroomId,
            @PathVariable Long messageId,
            @RequestBody MessageRequest request) {
        Message message = messageService.editMessage(
                chatroomId,
                messageId,
                request.getContent()
        );
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Boolean>> deleteMessage(
            @PathVariable Long chatroomId,
            @PathVariable Long messageId) {
        boolean success = messageService.deleteMessage(chatroomId, messageId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{messageId}/undo")
    public ResponseEntity<Map<String, Boolean>> undoLastAction(
            @PathVariable Long chatroomId,
            @PathVariable Long messageId) {
        boolean success = messageService.undoLastAction(chatroomId, messageId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);

        return ResponseEntity.ok(response);
    }

    // Helper class for request body
    static class MessageRequest {
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
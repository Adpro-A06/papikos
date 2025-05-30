package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatroomRestController {

    private final ChatroomServiceImpl chatroomService;
    private static final Logger logger = LoggerFactory.getLogger(ChatroomRestController.class);

    public ChatroomRestController(ChatroomServiceImpl chatroomService) {
        this.chatroomService = chatroomService;
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<Chatroom>> getChatroomsByRenterId(@PathVariable UUID renterId) {
        List<Chatroom> chatrooms = chatroomService.getChatroomsByRenterId(renterId);
        return ResponseEntity.ok(chatrooms);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Chatroom>> getChatroomsByOwnerId(@PathVariable UUID ownerId) {
        List<Chatroom> chatrooms = chatroomService.getChatroomsByOwnerId(ownerId);
        return ResponseEntity.ok(chatrooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chatroom> getChatroomById(@PathVariable UUID id) {
        try {
            Chatroom chatroom = chatroomService.getChatroomById(id);
            return ResponseEntity.ok(chatroom);
        } catch (RuntimeException e) {
            logger.error("Chatroom not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create/{kosId}")
    public ResponseEntity<Map<String, Object>> createChatroom(@PathVariable UUID kosId, @RequestBody ChatroomRequest request) {
        try {
            logger.info("Creating chatroom for kosId: {}, renterId: {}, ownerId: {}",
                    kosId, request.getRenterId(), request.getOwnerId());

            Chatroom chatroom = chatroomService.createChatroom(
                    request.getRenterId(),
                    request.getOwnerId(),
                    kosId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chatroomId", chatroom.getId().toString());
            response.put("renterId", chatroom.getRenterId().toString());
            response.put("ownerId", chatroom.getOwnerId().toString());
            response.put("propertyId", chatroom.getPropertyId().toString());
            response.put("message", "Chatroom created successfully");

            logger.info("Chatroom created successfully with id: {}", chatroom.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating chatroom: ", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create chatroom: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Getter
    @Setter
    static class ChatroomRequest {
        private UUID renterId;
        private UUID ownerId;
        private String propertyId;
    }
}
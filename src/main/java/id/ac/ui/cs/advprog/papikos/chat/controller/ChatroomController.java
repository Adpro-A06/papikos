package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatroomController {

    private final ChatroomService chatroomService;

    public ChatroomController(ChatroomService chatroomService) {
        this.chatroomService = chatroomService;
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<List<Chatroom>> getChatroomsByRenterId(@PathVariable Long renterId) {
        List<Chatroom> chatrooms = chatroomService.getChatroomsByRenterId(renterId);
        return ResponseEntity.ok(chatrooms);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Chatroom>> getChatroomsByOwnerId(@PathVariable Long ownerId) {
        List<Chatroom> chatrooms = chatroomService.getChatroomsByOwnerId(ownerId);
        return ResponseEntity.ok(chatrooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Chatroom> getChatroomById(@PathVariable Long id) {
        Chatroom chatroom = chatroomService.getChatroomById(id);
        return ResponseEntity.ok(chatroom);
    }

    @PostMapping
    public ResponseEntity<Chatroom> createChatroom(@RequestBody ChatroomRequest request) {
        Chatroom chatroom = chatroomService.createChatroom(
                request.getRenterId(),
                request.getOwnerId(),
                request.getPropertyId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(chatroom);
    }

    static class ChatroomRequest {
        private Long renterId;
        private Long ownerId;
        private Long propertyId;

        public Long getRenterId() {
            return renterId;
        }

        public void setRenterId(Long renterId) {
            this.renterId = renterId;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }

        public Long getPropertyId() {
            return propertyId;
        }

        public void setPropertyId(Long propertyId) {
            this.propertyId = propertyId;
        }
    }
}
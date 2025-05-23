package id.ac.ui.cs.advprog.papikos.chat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Chatroom {
    private UUID id;  // Changed from Long to UUID
    private UUID renterId;  // Changed from Long to UUID
    private UUID ownerId;  // Changed from Long to UUID
    private UUID propertyId;
    private LocalDateTime createdAt;
    private List<Message> messages;

    public Chatroom() {
        this.messages = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRenterId() {
        return renterId;
    }

    public void setRenterId(UUID renterId) {
        this.renterId = renterId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public Message getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.stream()
                .max(Comparator.comparing(Message::getTimestamp))
                .orElse(null);
    }

    public int getUnreadMessageCount(UUID userId) {  // Changed from Long to UUID
        return (int) messages.stream()
                .filter(m -> !m.getSenderId().equals(userId) && !m.isRead())
                .count();
    }
}

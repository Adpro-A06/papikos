package id.ac.ui.cs.advprog.papikos.chat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Chatroom {
    private UUID id;
    private UUID renterId;
    private UUID ownerId;
    private UUID propertyId;
    private LocalDateTime createdAt;
    private List<Message> messages = new ArrayList<>();

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

    public int getUnreadMessageCount(UUID userId) {
        return (int) messages.stream()
                .filter(m -> !m.getSenderId().equals(userId) && !m.isRead())
                .count();
    }
}
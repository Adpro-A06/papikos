package id.ac.ui.cs.advprog.papikos.chat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Message {
    private UUID id;  // Changed from Long to UUID
    private UUID senderId;  // Changed from Long to UUID
    private UUID chatroomId;  // Changed from Long to UUID
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead = false;
    private LocalDateTime readAt;
    private boolean isEdited = false;

    public Message() {
    }

    // Read receipt
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    // isEdited flag
    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }
}

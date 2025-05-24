package id.ac.ui.cs.advprog.papikos.chat.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Message {
    private UUID id;
    private UUID senderId;
    private UUID chatroomId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead = false;
    private LocalDateTime readAt;
    private boolean isEdited = false;
    private boolean isDeleted = false;

    // isEdited flag
    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    // isDeleted flag
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}

package id.ac.ui.cs.advprog.papikos.notification.model;

public class Notification {
    private String id;
    private String message;
    private NotificationType type;
    private User recipient;
    private boolean isRead;

    public Notification(String id, String message, NotificationType type, User recipient) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.recipient = recipient;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}

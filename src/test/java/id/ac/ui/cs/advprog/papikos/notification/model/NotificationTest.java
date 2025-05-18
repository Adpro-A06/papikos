package id.ac.ui.cs.advprog.papikos.notification.model;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class NotificationTest {

    private User recipient;
    private Notification notification;
    private final String id = UUID.randomUUID().toString();
    private final String message = "Test notification message";
    private final NotificationType type = NotificationType.ADMIN_BROADCAST;

    @BeforeEach
    void setUp() {
        recipient = new User("user@example.com", "P@ssw0rd!", Role.PENYEWA);
        notification = new Notification(id, message, type, recipient);
    }

    @Test
    void testConstructor() {
        assertEquals(id, notification.getId());
        assertEquals(message, notification.getMessage());
        assertEquals(type, notification.getType());
        assertEquals(recipient, notification.getRecipient());
        assertFalse(notification.isRead());
    }

    @Test
    void testGetAndSetId() {
        String newId = "notif-456";
        notification.setId(newId);
        assertEquals(newId, notification.getId());
    }

    @Test
    void testGetAndSetMessage() {
        String newMessage = "Updated notification message";
        notification.setMessage(newMessage);
        assertEquals(newMessage, notification.getMessage());
    }

    @Test
    void testGetAndSetType() {
        NotificationType newType = NotificationType.KOS_AVAILABLE;
        notification.setType(newType);
        assertEquals(newType, notification.getType());
    }

    @Test
    void testGetAndSetRecipient() {
        User newRecipient = new User("other@example.com", "P@ssword456", Role.PEMILIK_KOS);
        notification.setRecipient(newRecipient);
        assertEquals(newRecipient, notification.getRecipient());
    }

    @Test
    void testIsReadDefault() {
        assertFalse(notification.isRead());
    }

    @Test
    void testSetRead() {
        notification.setRead(true);
        assertTrue(notification.isRead());

        notification.setRead(false);
        assertFalse(notification.isRead());
    }
}
package id.ac.ui.cs.advprog.papikos.payment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTest {

    private Payment payment;
    private UUID fromUserId;
    private UUID toUserId;
    private BigDecimal amount;
    private TransactionType type;
    private PaymentStatus status;
    private UUID roomId;
    private String description;

    @BeforeEach
    void setUp() {
        // Hardcoded data for testing
        fromUserId = UUID.randomUUID();
        toUserId = UUID.randomUUID();
        amount = new BigDecimal("1000.50");
        type = TransactionType.TOPUP;
        status = PaymentStatus.SUCCESS;
        roomId = UUID.randomUUID();
        description = "Test Payment";

        // Create Payment object using constructor
        payment = new Payment(fromUserId, toUserId, amount, type, status, roomId, description);
    }

    @Test
    void testPaymentConstructorInitialization() {
        assertNotNull(payment.getId(), "Payment ID should be generated");
        assertEquals(fromUserId, payment.getFromUserId(), "From User ID should match");
        assertEquals(toUserId, payment.getToUserId(), "To User ID should match");
        assertEquals(amount, payment.getAmount(), "Amount should match");
        assertEquals(type, payment.getType(), "Transaction type should match");
        assertEquals(status, payment.getStatus(), "Payment status should match");
        assertEquals(roomId, payment.getRoomId(), "Room ID should match");
        assertEquals(description, payment.getDescription(), "Description should match");
        assertNotNull(payment.getTimestamp(), "Timestamp should be generated");
    }

    @Test
    void testSettersAndGetters() {
        // Modify fields using setters
        payment.setAmount(new BigDecimal("2000.00"));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setDescription("Updated Payment Description");

        // Validate updated fields using getters
        assertEquals(new BigDecimal("2000.00"), payment.getAmount(), "Amount should be updated");
        assertEquals(PaymentStatus.FAILED, payment.getStatus(), "Status should be updated");
        assertEquals("Updated Payment Description", payment.getDescription(), "Description should be updated");
    }

    @Test
    void testPaymentIdGeneration() {
        // Since the ID is randomly generated in the constructor, check if it's not null
        assertNotNull(payment.getId(), "Payment ID should be generated upon creation");
    }

    @Test
    void testConstructorSetsAllFields() {
        // Verify that all fields are initialized correctly via constructor
        assertEquals(fromUserId, payment.getFromUserId(), "From User ID mismatch");
        assertEquals(toUserId, payment.getToUserId(), "To User ID mismatch");
        assertEquals(amount, payment.getAmount(), "Amount mismatch");
        assertEquals(type, payment.getType(), "Transaction Type mismatch");
        assertEquals(status, payment.getStatus(), "Payment Status mismatch");
        assertEquals(roomId, payment.getRoomId(), "Room ID mismatch");
        assertEquals(description, payment.getDescription(), "Description mismatch");
        assertNotNull(payment.getTimestamp(), "Timestamp should not be null");
    }

    @Test
    void testTimestampIsSet() {
        // Check if timestamp is set upon creation
        assertNotNull(payment.getTimestamp(), "Timestamp should be set in the constructor");
        assertTrue(payment.getTimestamp().isBefore(LocalDateTime.now()) || payment.getTimestamp().isEqual(LocalDateTime.now()), "Timestamp should be in the past or present");
    }
}

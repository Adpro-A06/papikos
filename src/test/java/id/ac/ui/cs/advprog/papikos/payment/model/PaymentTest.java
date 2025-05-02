package id.ac.ui.cs.advprog.papikos.payment.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTest {

    @Test
    void createPayment_setsAllFields() {
        UUID id = UUID.randomUUID();
        UUID fromUserId = UUID.randomUUID();
        UUID toUserId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50000");
        TransactionType type = TransactionType.PAYMENT;
        LocalDateTime timestamp = LocalDateTime.now();
        PaymentStatus status = PaymentStatus.SUCCESS;

        Payment payment = new Payment(id, fromUserId, toUserId, amount, type, timestamp, status);

        assertEquals(id, payment.getId());
        assertEquals(fromUserId, payment.getFromUserId());
        assertEquals(toUserId, payment.getToUserId());
        assertEquals(amount, payment.getAmount());
        assertEquals(type, payment.getType());
        assertEquals(timestamp, payment.getTimestamp());
        assertEquals(status, payment.getStatus());
    }
}

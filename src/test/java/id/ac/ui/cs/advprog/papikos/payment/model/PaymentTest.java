package id.ac.ui.cs.advprog.papikos.payment.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentTest {

    @Test
    void createPayment_setsAllFields() {
        String id = "trx-001";
        String fromUserId = "user001";
        String toUserId = "user002";
        BigDecimal amount = new BigDecimal("50000");
        TransactionType type = TransactionType.PAYMENT;
        LocalDateTime timestamp = LocalDateTime.now();

        Payment payment = new Payment(id, fromUserId, toUserId, amount, type, timestamp);

        assertEquals(id, payment.getId());
        assertEquals(fromUserId, payment.getFromUserId());
        assertEquals(toUserId, payment.getToUserId());
        assertEquals(amount, payment.getAmount());
        assertEquals(type, payment.getType());
        assertEquals(timestamp, payment.getTimestamp());
    }
}

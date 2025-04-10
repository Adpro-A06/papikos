package id.ac.ui.cs.advprog.papikos.payment.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Payment {

    private final String id;
    private final String fromUserId;
    private final String toUserId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;

    public Payment(String id, String fromUserId, String toUserId,
                   BigDecimal amount, TransactionType type, LocalDateTime timestamp) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }
}

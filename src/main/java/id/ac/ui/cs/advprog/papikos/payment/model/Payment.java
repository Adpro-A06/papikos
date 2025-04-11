package id.ac.ui.cs.advprog.papikos.payment.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Payment {
    private final UUID id;
    private final UUID fromUserId;
    private final UUID toUserId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;
    private final PaymentStatus status;

    public Payment(UUID id, UUID fromUserId, UUID toUserId,
                   BigDecimal amount, TransactionType type,
                   LocalDateTime timestamp, PaymentStatus status) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
        this.status = status;
    }
}

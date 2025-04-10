package id.ac.ui.cs.advprog.papikos.payment.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Payment {

    String id;
    String fromUserId;
    String toUserId;
    BigDecimal amount;
    TransactionType type;
    LocalDateTime timestamp;

    public Payment(String id, String fromUserId, String toUserId,
                   BigDecimal amount, TransactionType type, LocalDateTime timestamp) {
    }
}

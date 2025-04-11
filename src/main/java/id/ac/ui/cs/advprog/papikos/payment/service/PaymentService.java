package id.ac.ui.cs.advprog.papikos.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    void topUp(UUID userId, BigDecimal amount);
    void pay(UUID fromUserId, UUID toUserId, BigDecimal amount);
}

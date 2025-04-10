package id.ac.ui.cs.advprog.papikos.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PaymentService {

    private final Map<String, BigDecimal> balances = new HashMap<>();

    public void topUp(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top up amount must be greater than zero");
        }

        BigDecimal current = balances.getOrDefault(userId, BigDecimal.ZERO);
        balances.put(userId, current.add(amount));
    }

    public BigDecimal getBalance(String userId) {
        return balances.getOrDefault(userId, BigDecimal.ZERO);
    }
}

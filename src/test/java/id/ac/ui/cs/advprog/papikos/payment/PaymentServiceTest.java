package id.ac.ui.cs.advprog.papikos.payment;

import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService();
    }

    @Test
    void topUp_fromZero() {
        String userId = "user001";

        assertEquals(BigDecimal.ZERO, service.getBalance(userId));

        service.topUp(userId, new BigDecimal("50000"));

        assertEquals(new BigDecimal("50000"), service.getBalance(userId));
    }

    @Test
    void topUp_addBalance() {
        String userId = "user001";

        service.topUp(userId, new BigDecimal("30000"));
        service.topUp(userId, new BigDecimal("20000"));

        assertEquals(new BigDecimal("50000"), service.getBalance(userId));
    }

    @Test
    void topUp_negativeAmount() {
        String userId = "user001";

        assertThrows(IllegalArgumentException.class, () -> {
            service.topUp(userId, new BigDecimal("-10000"));
        });
    }

    @Test
    void topUp_zeroAmount() {
        String userId = "user001";

        assertThrows(IllegalArgumentException.class, () -> {
            service.topUp(userId, BigDecimal.ZERO);
        });
    }
}

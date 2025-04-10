package id.ac.ui.cs.advprog.papikos.payment.service;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.repository.IPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    // topUp - Happy Path

    @Test
    void topUp_fromZero() {
        BigDecimal amount = new BigDecimal("50000");

        paymentService.topUp(userId, amount);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment saved = captor.getValue();
        assertEquals(userId, saved.getToUserId());
        assertNull(saved.getFromUserId());
        assertEquals(amount, saved.getAmount());
        assertEquals(TransactionType.TOPUP, saved.getType());
    }

    // topUp - Unhappy Path

    @Test
    void topUp_zeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.topUp(userId, BigDecimal.ZERO));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void topUp_negativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> paymentService.topUp(userId, new BigDecimal("-10000")));
        verify(paymentRepository, never()).save(any());
    }

    // pay - Happy Path

    @Test
    void pay_valid() {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150000");

        paymentService.pay(from, to, amount);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());

        Payment saved = captor.getValue();
        assertEquals(from, saved.getFromUserId());
        assertEquals(to, saved.getToUserId());
        assertEquals(amount, saved.getAmount());
        assertEquals(TransactionType.PAYMENT, saved.getType());
    }

    // pay - Unhappy Path

    @Test
    void pay_zeroAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.pay(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
        });
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void pay_negativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.pay(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("-50000"));
        });
        verify(paymentRepository, never()).save(any());
    }
}

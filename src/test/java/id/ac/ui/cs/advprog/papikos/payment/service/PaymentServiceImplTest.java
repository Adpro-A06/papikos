package id.ac.ui.cs.advprog.papikos.payment.service;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.repository.IPaymentRepository;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceImplTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID fromUserId;
    private UUID toUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fromUserId = UUID.randomUUID();
        toUserId = UUID.randomUUID();
    }

    @Test
    void testTopUpHappyPath() {
        BigDecimal amount = new BigDecimal("100");

        paymentService.topUp(fromUserId, amount);

        Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
        Mockito.verify(userRepository, Mockito.times(1)).updateBalance(fromUserId, amount);
    }

    @Test
    void testTopUpUnhappyPathAmountZero() {
        BigDecimal amount = BigDecimal.ZERO;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.topUp(fromUserId, amount);
        });
        assertEquals("Amount must be greater than zero", thrown.getMessage());
    }

    @Test
    void testTopUpUnhappyPathAmountNegative() {
        BigDecimal amount = new BigDecimal("-50");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.topUp(fromUserId, amount);
        });
        assertEquals("Amount must be greater than zero", thrown.getMessage());
    }

    @Test
    void testPayHappyPath() {
        BigDecimal amount = new BigDecimal("50");
        Mockito.when(userRepository.getBalance(fromUserId)).thenReturn(new BigDecimal("100"));

        paymentService.pay(fromUserId, toUserId, amount);

        Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
        Mockito.verify(userRepository, Mockito.times(1)).updateBalance(fromUserId, amount.negate());
        Mockito.verify(userRepository, Mockito.times(1)).updateBalance(toUserId, amount);
    }

    @Test
    void testPayUnhappyPathInsufficientBalance() {
        BigDecimal amount = new BigDecimal("150");
        Mockito.when(userRepository.getBalance(fromUserId)).thenReturn(new BigDecimal("100"));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.pay(fromUserId, toUserId, amount);
        });
        assertEquals("Insufficient balance", thrown.getMessage());
    }

    @Test
    void testPayUnhappyPathAmountZero() {
        BigDecimal amount = BigDecimal.ZERO;

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.pay(fromUserId, toUserId, amount);
        });
        assertEquals("Amount must be greater than zero", thrown.getMessage());
    }

    @Test
    void testPayUnhappyPathAmountNegative() {
        BigDecimal amount = new BigDecimal("-50");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.pay(fromUserId, toUserId, amount);
        });
        assertEquals("Amount must be greater than zero", thrown.getMessage());
    }
}

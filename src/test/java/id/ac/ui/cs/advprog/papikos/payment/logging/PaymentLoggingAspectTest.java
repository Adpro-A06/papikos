package id.ac.ui.cs.advprog.papikos.payment.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentLoggingAspectTest {

    @InjectMocks
    private PaymentLoggingAspect paymentLoggingAspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    private UUID userId1;
    private UUID userId2;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        amount = new BigDecimal("100.00");
    }

    @Test
    void testLogTopUpSuccess() throws Throwable {
        Object[] args = { userId1, amount };
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.proceed()).thenReturn("success");

        Object result = paymentLoggingAspect.logTopUp(proceedingJoinPoint);

        assertEquals("success", result);
        verify(proceedingJoinPoint).proceed();
        verify(proceedingJoinPoint, atLeastOnce()).getArgs();
    }

    @Test
    void testLogTopUpException() throws Throwable {
        Object[] args = { userId1, amount };
        RuntimeException testException = new RuntimeException("Test exception");

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.proceed()).thenThrow(testException);

        Exception exception = assertThrows(RuntimeException.class,
                () -> paymentLoggingAspect.logTopUp(proceedingJoinPoint));

        assertEquals(testException, exception);
        verify(proceedingJoinPoint).getArgs();
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    void testLogPaymentSuccess() throws Throwable {
        Object[] args = { userId1, userId2, amount };
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.proceed()).thenReturn("payment success");

        Object result = paymentLoggingAspect.logPayment(proceedingJoinPoint);

        assertEquals("payment success", result);
        verify(proceedingJoinPoint).proceed();
        verify(proceedingJoinPoint, atLeastOnce()).getArgs();
    }

    @Test
    void testLogPaymentException() throws Throwable {
        Object[] args = { userId1, userId2, amount };
        RuntimeException testException = new RuntimeException("Payment failed");

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.proceed()).thenThrow(testException);

        Exception exception = assertThrows(RuntimeException.class,
                () -> paymentLoggingAspect.logPayment(proceedingJoinPoint));

        assertEquals(testException, exception);
        verify(proceedingJoinPoint).getArgs();
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    void testLogServiceException() {
        Object[] args = { userId1, amount };
        Exception exception = new RuntimeException("Service exception");

        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("PaymentService.topUp()");

        assertDoesNotThrow(() -> paymentLoggingAspect.logServiceException(joinPoint, exception));

        verify(joinPoint).getArgs();
        verify(joinPoint).getSignature();
        verify(signature).toShortString();
    }
}
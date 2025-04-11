package id.ac.ui.cs.advprog.papikos.payment.repository;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRepositoryTest {

    @InjectMocks
    private PaymentRepository repository;

    @Test
    void shouldSaveValidPayment() {
        UUID id = UUID.randomUUID();
        UUID fromUserId = UUID.randomUUID();
        UUID toUserId = UUID.randomUUID();

        Payment payment = new Payment(id, fromUserId, toUserId,
                new BigDecimal("100000"), TransactionType.PAYMENT, LocalDateTime.now(), PaymentStatus.SUCCESS);

        repository.save(payment);
        List<Payment> all = repository.findAll();

        assertEquals(1, all.size());
        assertEquals(id, all.get(0).getId());
    }

    @Test
    void shouldSaveMultiplePayments() {
        repository.save(new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50000"), TransactionType.PAYMENT, LocalDateTime.now(), PaymentStatus.SUCCESS));
        repository.save(new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("60000"), TransactionType.TOPUP, LocalDateTime.now(), PaymentStatus.SUCCESS));

        List<Payment> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldReturnPaymentsForGivenUser() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        repository.save(new Payment(UUID.randomUUID(), user1, UUID.randomUUID(), new BigDecimal("70000"), TransactionType.PAYMENT, LocalDateTime.now(), PaymentStatus.SUCCESS));
        repository.save(new Payment(UUID.randomUUID(), user2, UUID.randomUUID(), new BigDecimal("80000"), TransactionType.PAYMENT, LocalDateTime.now(), PaymentStatus.SUCCESS));

        List<Payment> user1Payments = repository.findByUserId(user1);
        assertEquals(1, user1Payments.size());
        assertEquals(user1, user1Payments.get(0).getFromUserId());
    }

    @Test
    void shouldReturnEmptyListWhenNoPaymentSaved() {
        List<Payment> all = repository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenSavingNullPayment() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateId() {
        UUID id = UUID.randomUUID();
        Payment p1 = new Payment(id, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50000"), TransactionType.PAYMENT, LocalDateTime.now(), PaymentStatus.SUCCESS);
        Payment p2 = new Payment(id, UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("60000"), TransactionType.TOPUP, LocalDateTime.now(), PaymentStatus.SUCCESS);

        repository.save(p1);
        assertThrows(IllegalArgumentException.class, () -> repository.save(p2));
    }

    @Test
    void shouldReturnEmptyListIfUserHasNoTransaction() {
        UUID ghostUser = UUID.randomUUID();
        List<Payment> result = repository.findByUserId(ghostUser);
        assertTrue(result.isEmpty());
    }
}

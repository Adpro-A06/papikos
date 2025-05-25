package id.ac.ui.cs.advprog.papikos.payment.repository;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PaymentRepositoryTest {

    @Autowired
    private IPaymentRepository paymentRepository;

    private UUID user1Id;
    private UUID user2Id;
    private UUID paymentId1;
    private UUID paymentId2;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();
        paymentId1 = UUID.randomUUID();
        paymentId2 = UUID.randomUUID();
    }

    @Test
    void shouldSaveAndRetrievePayment() {
        Payment payment = new Payment(
                user1Id,
                user2Id,
                new BigDecimal("100000"),
                TransactionType.PAYMENT,
                PaymentStatus.SUCCESS,
                null,
                "Test payment"
        );
        payment.setId(paymentId1);

        paymentRepository.save(payment);

        Optional<Payment> retrieved = paymentRepository.findById(paymentId1);

        assertTrue(retrieved.isPresent());
        assertEquals(paymentId1, retrieved.get().getId());
        assertEquals(user1Id, retrieved.get().getFromUserId());
        assertEquals(user2Id, retrieved.get().getToUserId());
        assertEquals(new BigDecimal("100000"), retrieved.get().getAmount());
    }

    @Test
    void shouldFindByFromUserIdOrToUserId() {

        Payment payment1 = new Payment(
                user1Id,
                user2Id,
                new BigDecimal("50000"),
                TransactionType.PAYMENT,
                PaymentStatus.SUCCESS,
                null,
                "Payment from user1 to user2"
        );
        payment1.setId(paymentId1);

        Payment payment2 = new Payment(
                user2Id,
                user1Id,
                new BigDecimal("30000"),
                TransactionType.PAYMENT,
                PaymentStatus.SUCCESS,
                null,
                "Payment from user2 to user1"
        );
        payment2.setId(paymentId2);

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        List<Payment> user1Transactions = paymentRepository.findByFromUserIdOrToUserId(user1Id, user1Id);
        assertEquals(2, user1Transactions.size());

        List<Payment> user2Transactions = paymentRepository.findByFromUserIdOrToUserId(user2Id, user2Id);
        assertEquals(2, user2Transactions.size());
    }

    @Test
    void shouldFindByToUserIdEquals() {
        Payment payment1 = new Payment(
                user1Id,
                user2Id,
                new BigDecimal("50000"),
                TransactionType.PAYMENT,
                PaymentStatus.SUCCESS,
                null,
                "Payment to user2"
        );
        payment1.setId(paymentId1);

        paymentRepository.save(payment1);

        List<Payment> user2ReceivedPayments = paymentRepository.findByToUserIdEquals(user2Id);
        assertEquals(1, user2ReceivedPayments.size());
        assertEquals(user2Id, user2ReceivedPayments.get(0).getToUserId());

        List<Payment> randomUserReceivedPayments = paymentRepository.findByToUserIdEquals(UUID.randomUUID());
        assertEquals(0, randomUserReceivedPayments.size());
    }

    @Test
    void shouldFindByFromUserIdAndStatusOrToUserIdAndStatus() {
        Payment payment1 = new Payment(
                user1Id,
                user2Id,
                new BigDecimal("50000"),
                TransactionType.PAYMENT,
                PaymentStatus.SUCCESS,
                null,
                "Successful payment"
        );
        payment1.setId(paymentId1);

        Payment payment2 = new Payment(
                user2Id,
                user1Id,
                new BigDecimal("30000"),
                TransactionType.PAYMENT,
                PaymentStatus.PENDING,
                null,
                "Pending payment"
        );
        payment2.setId(paymentId2);

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        List<Payment> successTransactions = paymentRepository.findByFromUserIdAndStatusOrToUserIdAndStatus(
                user1Id, PaymentStatus.SUCCESS, user1Id, PaymentStatus.SUCCESS);
        assertEquals(1, successTransactions.size());
        assertEquals(PaymentStatus.SUCCESS, successTransactions.get(0).getStatus());

        List<Payment> pendingTransactions = paymentRepository.findByFromUserIdAndStatusOrToUserIdAndStatus(
                user1Id, PaymentStatus.PENDING, user1Id, PaymentStatus.PENDING);
        assertEquals(1, pendingTransactions.size());
        assertEquals(PaymentStatus.PENDING, pendingTransactions.get(0).getStatus());
    }

    @Test
    void shouldReturnEmptyListWhenNoPayments() {
        List<Payment> result = paymentRepository.findByFromUserIdOrToUserId(UUID.randomUUID(), UUID.randomUUID());
        assertTrue(result.isEmpty());
    }
}

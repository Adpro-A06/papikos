package id.ac.ui.cs.advprog.papikos.payment.service;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final IPaymentRepository paymentRepository;

    @Override
    public void topUp(UUID userId, BigDecimal amount) {
        validateAmount(amount);

        Payment payment = new Payment(
                UUID.randomUUID(),
                null,
                userId,
                amount,
                TransactionType.TOPUP,
                LocalDateTime.now(),
                PaymentStatus.SUCCESS
        );

        paymentRepository.save(payment);
    }

    @Override
    public void pay(UUID fromUserId, UUID toUserId, BigDecimal amount) {
        validateAmount(amount);

        Payment payment = new Payment(
                UUID.randomUUID(),
                fromUserId,
                toUserId,
                amount,
                TransactionType.PAYMENT,
                LocalDateTime.now(),
                PaymentStatus.SUCCESS
        );

        paymentRepository.save(payment);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}

package id.ac.ui.cs.advprog.papikos.payment.service;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.model.Wallet;
import id.ac.ui.cs.advprog.papikos.payment.monitoring.PaymentMetrics;
import id.ac.ui.cs.advprog.papikos.payment.repository.IPaymentRepository;
import id.ac.ui.cs.advprog.papikos.payment.repository.WalletRepository;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final IPaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final PaymentMetrics paymentMetrics;

    @Override
    @Transactional
    public void topUp(UUID userId, BigDecimal amount) {
        Timer.Sample sample = paymentMetrics.startPaymentProcessingTimer();
        log.info("Starting top-up operation for user {} with amount {}", userId, amount);

        try {
            validateAmount(amount);
            validateUserExists(userId);

            Wallet wallet = getOrCreateWallet(userId);

            Payment payment = new Payment(
                    null,
                    userId,
                    amount,
                    TransactionType.TOPUP,
                    PaymentStatus.SUCCESS,
                    null,
                    "Top-up balance"
            );

            wallet.addBalance(amount);

            walletRepository.save(wallet);
            paymentRepository.save(payment);

            paymentMetrics.recordTopUp(amount);
            log.info("Completed top-up operation for user {} with amount {}", userId, amount);
        } catch (Exception e) {
            log.error("Failed to process top-up for user {}: {}", userId, e.getMessage());
            paymentMetrics.recordFailedPayment();
            throw e;
        } finally {
            paymentMetrics.stopPaymentProcessingTimer(sample);
        }
    }

    @Override
    public BigDecimal getBalance(UUID userId) {
        log.debug("Getting balance for user {}", userId);
        validateUserExists(userId);

        Optional<Wallet> wallet = walletRepository.findById(userId);
        BigDecimal balance = wallet.map(Wallet::getBalance).orElse(BigDecimal.ZERO);
        log.debug("Balance for user {}: {}", userId, balance);
        return balance;
    }

    @Override
    @Transactional
    public void pay(UUID fromUserId, UUID toUserId, BigDecimal amount) {
        Timer.Sample sample = paymentMetrics.startPaymentProcessingTimer();
        log.info("Starting payment from user {} to user {} with amount {}", fromUserId, toUserId, amount);

        try {
            validateAmount(amount);
            validateUserExists(fromUserId);
            validateUserExists(toUserId);

            Wallet fromWallet = getOrCreateWallet(fromUserId);
            Wallet toWallet = getOrCreateWallet(toUserId);

            if (fromWallet.getBalance().compareTo(amount) < 0) {
                log.warn("Insufficient balance for user {}: requested {} but has {}",
                        fromUserId, amount, fromWallet.getBalance());
                paymentMetrics.recordFailedPayment();
                throw new IllegalArgumentException("Saldo tidak mencukupi");
            }

            Payment payment = new Payment(
                    fromUserId,
                    toUserId,
                    amount,
                    TransactionType.PAYMENT,
                    PaymentStatus.SUCCESS,
                    null,
                    "Pembayaran kos"
            );

            fromWallet.subtractBalance(amount);
            toWallet.addBalance(amount);

            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);
            paymentRepository.save(payment);

            paymentMetrics.recordPayment(amount);
            log.info("Completed payment from user {} to user {} with amount {}",
                    fromUserId, toUserId, amount);
        } catch (Exception e) {
            log.error("Failed to process payment from user {} to user {}: {}",
                    fromUserId, toUserId, e.getMessage());
            paymentMetrics.recordFailedPayment();
            throw e;
        } finally {
            paymentMetrics.stopPaymentProcessingTimer(sample);
        }
    }

    @Override
    public List<Payment> filterTransactions(UUID userId, LocalDate startDate, LocalDate endDate, TransactionType type) {
        log.debug("Filtering transactions for user {} from {} to {} with type {}",
                userId, startDate, endDate, type);
        List<Payment> transactions = paymentRepository.findByFromUserIdOrToUserId(userId, userId);

        return transactions.stream()
                .filter(payment -> {
                    boolean dateFilter = true;
                    if (startDate != null && endDate != null) {
                        LocalDateTime paymentDate = payment.getTimestamp();
                        dateFilter = !paymentDate.toLocalDate().isBefore(startDate) &&
                                !paymentDate.toLocalDate().isAfter(endDate);
                    }

                    boolean typeFilter = true;
                    if (type != null) {
                        typeFilter = payment.getType() == type;
                    }

                    return dateFilter && typeFilter;
                })
                .sorted((p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()))
                .collect(Collectors.toList());
    }

    private Wallet getOrCreateWallet(UUID userId) {
        return walletRepository.findById(userId)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet(userId);
                    return walletRepository.save(newWallet);
                });
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih besar dari nol");
        }
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User dengan ID " + userId + " tidak ditemukan");
        }
    }

    @Override
    public List<Payment> getUserTransactions(UUID userId) {
        validateUserExists(userId);
        return paymentRepository.findByFromUserIdOrToUserId(userId, userId);
    }
}


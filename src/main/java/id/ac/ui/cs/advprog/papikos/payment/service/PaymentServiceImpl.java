package id.ac.ui.cs.advprog.papikos.payment.service;

import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.model.Wallet;
import id.ac.ui.cs.advprog.papikos.payment.repository.IPaymentRepository;
import id.ac.ui.cs.advprog.papikos.payment.repository.WalletRepository;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final IPaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void topUp(UUID userId, BigDecimal amount) {
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
    }

    @Override
    public BigDecimal getBalance(UUID userId) {
        validateUserExists(userId);

        Optional<Wallet> wallet = walletRepository.findById(userId);
        return wallet.map(Wallet::getBalance).orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void pay(UUID fromUserId, UUID toUserId, BigDecimal amount) {
        validateAmount(amount);
        validateUserExists(fromUserId);
        validateUserExists(toUserId);

        Wallet fromWallet = getOrCreateWallet(fromUserId);
        Wallet toWallet = getOrCreateWallet(toUserId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
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
    }

    @Override
    public List<Payment> filterTransactions(UUID userId, LocalDate startDate, LocalDate endDate, TransactionType type) {
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


    @Async("paymentTaskExecutor")
    @Override
    public CompletableFuture<Void> topUpAsync(UUID userId, BigDecimal amount) {
        return CompletableFuture.runAsync(() -> {
            log.info("Processing asynchronous top-up for user: {}, amount: {}", userId, amount);
            try {
                topUp(userId, amount);
                log.info("Asynchronous top-up completed successfully for user: {}", userId);
            } catch (Exception e) {
                log.error("Error during asynchronous top-up for user: {}", userId, e);
                throw e;
            }
        });
    }

    @Async("paymentTaskExecutor")
    @Override
    public CompletableFuture<Void> payAsync(UUID fromUserId, UUID toUserId, BigDecimal amount) {
        return CompletableFuture.runAsync(() -> {
            log.info("Processing asynchronous payment from user: {} to user: {}, amount: {}",
                    fromUserId, toUserId, amount);
            try {
                pay(fromUserId, toUserId, amount);
                log.info("Asynchronous payment completed successfully from user: {} to user: {}",
                        fromUserId, toUserId);
            } catch (Exception e) {
                log.error("Error during asynchronous payment from user: {} to user: {}",
                        fromUserId, toUserId, e);
                throw e;
            }
        });
    }

    @Async("paymentTaskExecutor")
    @Override
    public CompletableFuture<BigDecimal> getBalanceAsync(UUID userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Getting balance asynchronously for user: {}", userId);
            try {
                BigDecimal balance = getBalance(userId);
                log.info("Asynchronously retrieved balance for user: {}", userId);
                return balance;
            } catch (Exception e) {
                log.error("Error during asynchronous balance retrieval for user: {}", userId, e);
                throw e;
            }
        });
    }

    @Async("paymentTaskExecutor")
    @Override
    public CompletableFuture<List<Payment>> getUserTransactionsAsync(UUID userId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Retrieving transactions asynchronously for user: {}", userId);
            try {
                List<Payment> transactions = getUserTransactions(userId);
                log.info("Asynchronously retrieved {} transactions for user: {}",
                        transactions.size(), userId);
                return transactions;
            } catch (Exception e) {
                log.error("Error during asynchronous transaction retrieval for user: {}", userId, e);
                throw e;
            }
        });
    }

    @Async("paymentTaskExecutor")
    @Override
    public CompletableFuture<List<Payment>> filterTransactionsAsync(
            UUID userId, LocalDate startDate, LocalDate endDate, TransactionType type) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Filtering transactions asynchronously for user: {}", userId);
            try {
                List<Payment> transactions = filterTransactions(userId, startDate, endDate, type);
                log.info("Asynchronously filtered {} transactions for user: {}",
                        transactions.size(), userId);
                return transactions;
            } catch (Exception e) {
                log.error("Error during asynchronous transaction filtering for user: {}", userId, e);
                throw e;
            }
        });
    }
}

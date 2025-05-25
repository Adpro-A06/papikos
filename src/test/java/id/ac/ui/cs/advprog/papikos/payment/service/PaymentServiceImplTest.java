package id.ac.ui.cs.advprog.papikos.payment.service;

import id.ac.ui.cs.advprog.papikos.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.PaymentStatus;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.model.Wallet;
import id.ac.ui.cs.advprog.papikos.payment.repository.IPaymentRepository;
import id.ac.ui.cs.advprog.papikos.payment.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private IPaymentRepository paymentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<Wallet> walletCaptor;

    private UUID userId;
    private UUID otherUserId;
    private Wallet userWallet;
    private Wallet otherWallet;
    private final BigDecimal initialBalance = new BigDecimal("100000");
    private final BigDecimal topUpAmount = new BigDecimal("50000");
    private final BigDecimal paymentAmount = new BigDecimal("75000");

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        userWallet = new Wallet(userId);
        userWallet.addBalance(initialBalance);

        otherWallet = new Wallet(otherUserId);
    }

    @Test
    void topUp_withValidAmount_shouldSucceed() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));

        paymentService.topUp(userId, topUpAmount);

        verify(walletRepository).save(walletCaptor.capture());
        verify(paymentRepository).save(paymentCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();
        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(initialBalance.add(topUpAmount), savedWallet.getBalance());

        assertNull(savedPayment.getFromUserId());
        assertEquals(userId, savedPayment.getToUserId());
        assertEquals(topUpAmount, savedPayment.getAmount());
        assertEquals(TransactionType.TOPUP, savedPayment.getType());
        assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus());
        assertEquals("Top-up balance", savedPayment.getDescription());
    }

    @Test
    void topUp_withNegativeAmount_shouldThrowException() {
        BigDecimal negativeAmount = new BigDecimal("-100");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.topUp(userId, negativeAmount)
        );

        assertEquals("Jumlah harus lebih besar dari nol", exception.getMessage());
        verify(walletRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void topUp_withZeroAmount_shouldThrowException() {
        BigDecimal zeroAmount = BigDecimal.ZERO;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.topUp(userId, zeroAmount)
        );

        assertEquals("Jumlah harus lebih besar dari nol", exception.getMessage());
        verify(walletRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void topUp_withNonExistentUser_shouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.topUp(userId, topUpAmount)
        );

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
        verify(walletRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void topUpAsync_withValidAmount_shouldSucceed() throws ExecutionException, InterruptedException {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));

        CompletableFuture<Void> future = paymentService.topUpAsync(userId, topUpAmount);
        future.get();

        verify(walletRepository).save(any(Wallet.class));
        verify(paymentRepository).save(any(Payment.class));
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void topUpAsync_withInvalidAmount_shouldCompleteExceptionally() {
        BigDecimal negativeAmount = new BigDecimal("-100");

        CompletableFuture<Void> future = paymentService.topUpAsync(userId, negativeAmount);

        assertThrows(ExecutionException.class, future::get);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void pay_withSufficientFunds_shouldSucceed() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));
        when(walletRepository.findById(otherUserId)).thenReturn(Optional.of(otherWallet));

        paymentService.pay(userId, otherUserId, paymentAmount);

        verify(walletRepository, times(2)).save(walletCaptor.capture());
        verify(paymentRepository).save(paymentCaptor.capture());

        List<Wallet> savedWallets = walletCaptor.getAllValues();
        Payment savedPayment = paymentCaptor.getValue();

        assertEquals(initialBalance.subtract(paymentAmount), savedWallets.get(0).getBalance());
        assertEquals(paymentAmount, savedWallets.get(1).getBalance());

        assertEquals(userId, savedPayment.getFromUserId());
        assertEquals(otherUserId, savedPayment.getToUserId());
        assertEquals(paymentAmount, savedPayment.getAmount());
        assertEquals(TransactionType.PAYMENT, savedPayment.getType());
        assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus());
    }

    @Test
    void pay_withInsufficientFunds_shouldThrowException() {
        BigDecimal tooLargeAmount = new BigDecimal("200000");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));
        when(walletRepository.findById(otherUserId)).thenReturn(Optional.of(otherWallet));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.pay(userId, otherUserId, tooLargeAmount)
        );

        assertEquals("Saldo tidak mencukupi", exception.getMessage());
        verify(walletRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void pay_withNonExistentRecipient_shouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(otherUserId)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.pay(userId, otherUserId, paymentAmount)
        );

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
        verify(walletRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payAsync_withSufficientFunds_shouldSucceed() throws ExecutionException, InterruptedException {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));
        when(walletRepository.findById(otherUserId)).thenReturn(Optional.of(otherWallet));

        CompletableFuture<Void> future = paymentService.payAsync(userId, otherUserId, paymentAmount);
        future.get();

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(paymentRepository).save(any(Payment.class));
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void payAsync_withInsufficientFunds_shouldCompleteExceptionally() {
        BigDecimal tooLargeAmount = new BigDecimal("200000");
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));
        when(walletRepository.findById(otherUserId)).thenReturn(Optional.of(otherWallet));

        CompletableFuture<Void> future = paymentService.payAsync(userId, otherUserId, tooLargeAmount);

        assertThrows(ExecutionException.class, future::get);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void getBalance_withExistingWallet_shouldReturnBalance() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));

        BigDecimal balance = paymentService.getBalance(userId);

        assertEquals(initialBalance, balance);
    }

    @Test
    void getBalance_withNoWallet_shouldReturnZero() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.empty());

        BigDecimal balance = paymentService.getBalance(userId);

        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void getBalance_withNonExistentUser_shouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                paymentService.getBalance(userId)
        );

        assertTrue(exception.getMessage().contains("tidak ditemukan"));
    }

    @Test
    void getBalanceAsync_withExistingWallet_shouldReturnBalance() throws ExecutionException, InterruptedException {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(walletRepository.findById(userId)).thenReturn(Optional.of(userWallet));

        CompletableFuture<BigDecimal> future = paymentService.getBalanceAsync(userId);
        BigDecimal balance = future.get();

        assertEquals(initialBalance, balance);
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void getBalanceAsync_withNonExistentUser_shouldCompleteExceptionally() {
        when(userRepository.existsById(userId)).thenReturn(false);

        CompletableFuture<BigDecimal> future = paymentService.getBalanceAsync(userId);

        assertThrows(ExecutionException.class, future::get);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void getUserTransactions_shouldReturnAllTransactions() {
        List<Payment> transactions = createTestTransactions();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        List<Payment> result = paymentService.getUserTransactions(userId);

        assertEquals(transactions, result);
    }

    @Test
    void getUserTransactionsAsync_shouldReturnAllTransactions() throws ExecutionException, InterruptedException {
        List<Payment> transactions = createTestTransactions();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        CompletableFuture<List<Payment>> future = paymentService.getUserTransactionsAsync(userId);
        List<Payment> result = future.get();

        assertEquals(transactions, result);
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void getUserTransactionsAsync_withNonExistentUser_shouldCompleteExceptionally() {
        when(userRepository.existsById(userId)).thenReturn(false);

        CompletableFuture<List<Payment>> future = paymentService.getUserTransactionsAsync(userId);

        assertThrows(ExecutionException.class, future::get);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void filterTransactions_withNoFilters_shouldReturnAllTransactions() {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        List<Payment> result = paymentService.filterTransactions(userId, null, null, null);

        assertEquals(3, result.size());
    }

    @Test
    void filterTransactions_byDateRange_shouldFilterCorrectly() {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Payment> result = paymentService.filterTransactions(userId, yesterday, tomorrow, null);

        assertEquals(1, result.size());
        assertEquals(TransactionType.PAYMENT, result.get(0).getType());
    }

    @Test
    void filterTransactions_byType_shouldFilterCorrectly() {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        List<Payment> result = paymentService.filterTransactions(userId, null, null, TransactionType.TOPUP);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == TransactionType.TOPUP));
    }

    @Test
    void filterTransactions_byDateAndType_shouldFilterCorrectly() {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Payment> result = paymentService.filterTransactions(userId, yesterday, tomorrow, TransactionType.PAYMENT);

        assertEquals(1, result.size());
        assertEquals(TransactionType.PAYMENT, result.get(0).getType());
    }

    @Test
    void filterTransactionsAsync_withNoFilters_shouldReturnAllTransactions() throws ExecutionException, InterruptedException {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        CompletableFuture<List<Payment>> future = paymentService.filterTransactionsAsync(userId, null, null, null);
        List<Payment> result = future.get();

        assertEquals(3, result.size());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void filterTransactionsAsync_byDateRange_shouldFilterCorrectly() throws ExecutionException, InterruptedException {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        CompletableFuture<List<Payment>> future = paymentService.filterTransactionsAsync(userId, yesterday, tomorrow, null);
        List<Payment> result = future.get();

        assertEquals(1, result.size());
        assertEquals(TransactionType.PAYMENT, result.get(0).getType());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void filterTransactionsAsync_byType_shouldFilterCorrectly() throws ExecutionException, InterruptedException {
        List<Payment> transactions = createTestTransactions();

        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        CompletableFuture<List<Payment>> future = paymentService.filterTransactionsAsync(userId, null, null, TransactionType.TOPUP);
        List<Payment> result = future.get();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getType() == TransactionType.TOPUP));
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    private List<Payment> createTestTransactions() {
        List<Payment> transactions = new ArrayList<>();

        Payment topUpLastWeek = new Payment(
                null,
                userId,
                new BigDecimal("50000"),
                TransactionType.TOPUP,
                PaymentStatus.SUCCESS,
                null,
                "Top-up from last week"
        );
        topUpLastWeek.setTimestamp(LocalDateTime.now().minusDays(7));
        transactions.add(topUpLastWeek);

        Payment paymentToday = new Payment(
                userId,
                otherUserId,
                new BigDecimal("30000"),
                TransactionType.PAYMENT,
                PaymentStatus.SUCCESS,
                null,
                "Payment today"
        );
        paymentToday.setTimestamp(LocalDateTime.now());
        transactions.add(paymentToday);

        Payment topUpNextWeek = new Payment(
                null,
                userId,
                new BigDecimal("70000"),
                TransactionType.TOPUP,
                PaymentStatus.SUCCESS,
                null,
                "Top-up for next week"
        );
        topUpNextWeek.setTimestamp(LocalDateTime.now().plusDays(7));
        transactions.add(topUpNextWeek);

        return transactions;
    }
}
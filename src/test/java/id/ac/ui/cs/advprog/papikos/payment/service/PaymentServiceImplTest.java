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
    void getUserTransactions_shouldReturnAllTransactions() {
        List<Payment> transactions = createTestTransactions();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(paymentRepository.findByFromUserIdOrToUserId(userId, userId)).thenReturn(transactions);

        List<Payment> result = paymentService.getUserTransactions(userId);

        assertEquals(transactions, result);
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



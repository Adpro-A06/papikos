package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private AuthService authService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private PaymentController paymentController;

    private UUID testUserId;
    private UUID pemilikKosId;
    private User testUser;
    private User pemilikKosUser;
    private BigDecimal balance;
    private List<User> pemilikKosList;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        pemilikKosId = UUID.randomUUID();

        testUser = new User("penyewa@example.com", "Password123!", Role.PENYEWA);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (Exception e) {
        }

        pemilikKosUser = new User("pemilik@example.com", "Password123!", Role.PEMILIK_KOS);
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pemilikKosUser, pemilikKosId);
        } catch (Exception e) {
        }

        balance = new BigDecimal("100000");

        pemilikKosList = new ArrayList<>();
        pemilikKosList.add(pemilikKosUser);
    }

    @Test
    void showTopUpForm_whenUserLoggedIn_shouldShowTopUpPage() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);

        String viewName = paymentController.showTopUpForm(session, model, redirectAttributes);

        assertEquals("payment/TopUp", viewName);
        verify(model).addAttribute("balance", balance);
        verify(model).addAttribute("userId", testUserId);
        verify(model).addAttribute("user", testUser);
    }

    @Test
    void showTopUpForm_whenUserNotLoggedIn_shouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        String viewName = paymentController.showTopUpForm(session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }

    @Test
    void showTopUpForm_whenUserNotPenyewa_shouldRedirectToHome() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        String viewName = paymentController.showTopUpForm(session, model, redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
    }

    @Test
    void topUp_whenValidRequest_shouldProcessSuccessfully() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUp(testUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(paymentService).topUp(testUserId, amount);
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Top-up berhasil"));
    }

    @Test
    void topUp_whenUserNotLoggedIn_shouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUp(testUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
        verify(paymentService, never()).topUp(any(), any());
    }

    @Test
    void topUp_whenDifferentUser_shouldShowError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        UUID differentUserId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUp(differentUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/payment/topup", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda hanya dapat top-up ke akun Anda sendiri");
        verify(paymentService, never()).topUp(any(), any());
    }

    @Test
    void topUp_whenInvalidAmount_shouldShowError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        BigDecimal negativeAmount = new BigDecimal("-100");
        doThrow(new IllegalArgumentException("Jumlah top-up tidak valid"))
                .when(paymentService).topUp(testUserId, negativeAmount);

        String viewName = paymentController.topUp(testUserId, negativeAmount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Jumlah top-up tidak valid");
    }

    @Test
    void topUpAsync_whenValidRequest_shouldProcessSuccessfully() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.topUpAsync(any(UUID.class), any(BigDecimal.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUpAsync(testUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(paymentService).topUpAsync(testUserId, amount);
        verify(redirectAttributes).addFlashAttribute(eq("info"), contains("Permintaan top-up sedang diproses"));
    }

    @Test
    void topUpAsync_whenUserNotLoggedIn_shouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUpAsync(testUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
        verify(paymentService, never()).topUpAsync(any(), any());
    }

    @Test
    void topUpAsync_whenDifferentUser_shouldShowError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        UUID differentUserId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUpAsync(differentUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/payment/topup", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda hanya dapat top-up ke akun Anda sendiri");
        verify(paymentService, never()).topUpAsync(any(), any());
    }

    @Test
    void topUpAsync_whenUserNotPenyewa_shouldRedirectToHome() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUpAsync(testUserId, amount, session, redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
        verify(paymentService, never()).topUpAsync(any(), any());
    }

    @Test
    void showPaymentForm_whenUserLoggedIn_shouldShowPaymentForm() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);
        when(authService.findAllPemilikKos()).thenReturn(pemilikKosList);

        String viewName = paymentController.showPaymentForm(null, null, null, session, model, redirectAttributes);

        assertEquals("payment/PaymentForm", viewName);
        verify(model).addAttribute("balance", balance);
        verify(model).addAttribute("fromUserId", testUserId);
        verify(model).addAttribute("allPemilikKos", pemilikKosList);
    }

    @Test
    void showPaymentForm_withPrefilledData_shouldShowPaymentFormWithPrefills() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);
        when(authService.findAllPemilikKos()).thenReturn(pemilikKosList);
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        BigDecimal prefilledAmount = new BigDecimal("75000");
        String description = "Pembayaran sewa";

        String viewName = paymentController.showPaymentForm(
                pemilikKosId, prefilledAmount, description, session, model, redirectAttributes);

        assertEquals("payment/PaymentForm", viewName);
        verify(model).addAttribute("toUserId", pemilikKosId);
        verify(model).addAttribute("prefilledAmount", prefilledAmount);
        verify(model).addAttribute("prefilledDescription", description);
    }

    @Test
    void pay_whenValidRequest_shouldProcessSuccessfully() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        BigDecimal payAmount = new BigDecimal("50000");

        String viewName = paymentController.pay(testUserId, pemilikKosId, payAmount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(paymentService).pay(testUserId, pemilikKosId, payAmount);
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Pembayaran berhasil"));
    }

    @Test
    void pay_whenInsufficientBalance_shouldShowError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        BigDecimal exceedingAmount = new BigDecimal("999999");
        doThrow(new IllegalArgumentException("Saldo tidak mencukupi"))
                .when(paymentService).pay(testUserId, pemilikKosId, exceedingAmount);

        String viewName = paymentController.pay(testUserId, pemilikKosId, exceedingAmount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Saldo tidak mencukupi");
    }

    @Test
    void payAsync_whenValidRequest_shouldProcessSuccessfully() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.payAsync(any(UUID.class), any(UUID.class), any(BigDecimal.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        BigDecimal payAmount = new BigDecimal("50000");

        String viewName = paymentController.payAsync(testUserId, pemilikKosId, payAmount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(paymentService).payAsync(testUserId, pemilikKosId, payAmount);
        verify(redirectAttributes).addFlashAttribute(eq("info"), contains("Permintaan pembayaran sedang diproses"));
    }

    @Test
    void payAsync_whenUserNotLoggedIn_shouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        BigDecimal payAmount = new BigDecimal("50000");

        String viewName = paymentController.payAsync(testUserId, pemilikKosId, payAmount, session, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
        verify(paymentService, never()).payAsync(any(), any(), any());
    }

    @Test
    void payAsync_whenDifferentUser_shouldShowError() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        UUID differentUserId = UUID.randomUUID();
        BigDecimal payAmount = new BigDecimal("50000");

        String viewName = paymentController.payAsync(differentUserId, pemilikKosId, payAmount, session, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Anda hanya dapat melakukan pembayaran dari akun Anda sendiri");
        verify(paymentService, never()).payAsync(any(), any(), any());
    }

    @Test
    void payAsync_whenUserNotPenyewa_shouldRedirectToHome() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        BigDecimal payAmount = new BigDecimal("50000");

        String viewName = paymentController.payAsync(testUserId, pemilikKosId, payAmount, session, redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
        verify(paymentService, never()).payAsync(any(), any(), any());
    }

    @Test
    void showWallet_whenUserLoggedIn_shouldShowWallet() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        List<Payment> transactions = new ArrayList<>();
        when(paymentService.filterTransactions(eq(testUserId), isNull(), isNull(), isNull()))
                .thenReturn(transactions);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);

        String viewName = paymentController.showWallet(null, null, null, session, model, redirectAttributes);

        assertEquals("payment/Wallet", viewName);
        verify(model).addAttribute("transactions", transactions);
        verify(model).addAttribute("balance", balance);
    }

    @Test
    void showWallet_withFilters_shouldShowFilteredTransactions() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        String transactionType = TransactionType.PAYMENT.name();

        List<Payment> filteredTransactions = new ArrayList<>();
        when(paymentService.filterTransactions(testUserId, startDate, endDate, TransactionType.PAYMENT))
                .thenReturn(filteredTransactions);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);

        String viewName = paymentController.showWallet(startDate, endDate, transactionType,
                session, model, redirectAttributes);

        assertEquals("payment/Wallet", viewName);
        verify(paymentService).filterTransactions(testUserId, startDate, endDate, TransactionType.PAYMENT);
        verify(model).addAttribute("transactions", filteredTransactions);
    }

    @Test
    void showWalletAsync_whenUserLoggedIn_shouldShowWalletLoading() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.filterTransactionsAsync(any(UUID.class), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));
        when(paymentService.getBalanceAsync(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(balance));

        String viewName = paymentController.showWalletAsync(null, null, null, session, model, redirectAttributes);

        assertEquals("payment/WalletLoading", viewName);
        verify(model).addAttribute("user", testUser);
        verify(model).addAttribute("loadingTransactions", true);
        verify(model).addAttribute(eq("requestId"), any(String.class));
        verify(session).setAttribute(startsWith("transaction_request_"), any(CompletableFuture.class));
        verify(session).setAttribute(startsWith("balance_request_"), any(CompletableFuture.class));
    }

    @Test
    void showWalletAsync_whenUserNotLoggedIn_shouldRedirectToLogin() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        String viewName = paymentController.showWalletAsync(null, null, null, session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
        verify(paymentService, never()).filterTransactionsAsync(any(), any(), any(), any());
    }

    @Test
    void showWalletAsync_withFilters_shouldShowWalletLoadingWithFilters() {
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.filterTransactionsAsync(any(UUID.class), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));
        when(paymentService.getBalanceAsync(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(balance));

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        String transactionType = TransactionType.PAYMENT.name();

        String viewName = paymentController.showWalletAsync(startDate, endDate, transactionType, session, model, redirectAttributes);

        assertEquals("payment/WalletLoading", viewName);
        verify(model).addAttribute("startDate", startDate);
        verify(model).addAttribute("endDate", endDate);
        verify(model).addAttribute("transactionType", transactionType);
        verify(paymentService).filterTransactionsAsync(testUserId, startDate, endDate, TransactionType.PAYMENT);
    }

    @Test
    void checkAsyncWalletStatus_whenComplete_shouldReturnComplete() {
        String requestId = "test-request-id";
        CompletableFuture<List<Payment>> transactionsFuture = CompletableFuture.completedFuture(new ArrayList<>());
        CompletableFuture<BigDecimal> balanceFuture = CompletableFuture.completedFuture(balance);

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(transactionsFuture);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(balanceFuture);

        String status = paymentController.checkAsyncWalletStatus(requestId, session);

        assertEquals("complete", status);
    }

    @Test
    void checkAsyncWalletStatus_whenProcessing_shouldReturnProcessing() {
        String requestId = "test-request-id";
        CompletableFuture<List<Payment>> transactionsFuture = new CompletableFuture<>();
        CompletableFuture<BigDecimal> balanceFuture = new CompletableFuture<>();

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(transactionsFuture);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(balanceFuture);

        String status = paymentController.checkAsyncWalletStatus(requestId, session);

        assertEquals("processing", status);
    }

    @Test
    void checkAsyncWalletStatus_whenNotFound_shouldReturnError() {
        String requestId = "non-existent-request";

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(null);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(null);

        String status = paymentController.checkAsyncWalletStatus(requestId, session);

        assertEquals("error", status);
    }

    @Test
    void checkAsyncWalletStatus_whenOnlyTransactionsComplete_shouldReturnProcessing() {
        String requestId = "test-request-id";
        CompletableFuture<List<Payment>> transactionsFuture = CompletableFuture.completedFuture(new ArrayList<>());
        CompletableFuture<BigDecimal> balanceFuture = new CompletableFuture<>();

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(transactionsFuture);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(balanceFuture);

        String status = paymentController.checkAsyncWalletStatus(requestId, session);

        assertEquals("processing", status);
    }

    @Test
    void getAsyncWalletResult_whenRequestExists_shouldReturnWalletView() {
        String requestId = "test-request-id";
        List<Payment> transactions = new ArrayList<>();
        CompletableFuture<List<Payment>> transactionsFuture = CompletableFuture.completedFuture(transactions);
        CompletableFuture<BigDecimal> balanceFuture = CompletableFuture.completedFuture(balance);

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(transactionsFuture);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(balanceFuture);
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        String viewName = paymentController.getAsyncWalletResult(requestId, session, model, redirectAttributes);

        assertEquals("payment/Wallet", viewName);
        verify(model).addAttribute("transactions", transactions);
        verify(model).addAttribute("balance", balance);
        verify(model).addAttribute("asyncLoaded", true);
        verify(session).removeAttribute("transaction_request_" + requestId);
        verify(session).removeAttribute("balance_request_" + requestId);
    }

    @Test
    void getAsyncWalletResult_whenRequestNotFound_shouldRedirectToWallet() {
        String requestId = "non-existent-request";

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(null);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(null);

        String viewName = paymentController.getAsyncWalletResult(requestId, session, model, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Permintaan tidak ditemukan atau sudah kedaluwarsa");
    }

    @Test
    void getAsyncWalletResult_withPemilikKosUser_shouldIncludePenyewaEmails() {
        String requestId = "test-request-id";
        List<Payment> transactions = new ArrayList<>();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setFromUserId(testUserId);
        payment.setToUserId(pemilikKosId);
        payment.setAmount(new BigDecimal("50000"));
        payment.setType(TransactionType.PAYMENT);
        transactions.add(payment);

        CompletableFuture<List<Payment>> transactionsFuture = CompletableFuture.completedFuture(transactions);
        CompletableFuture<BigDecimal> balanceFuture = CompletableFuture.completedFuture(balance);

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(transactionsFuture);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(balanceFuture);
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);
        when(authService.findById(testUserId)).thenReturn(testUser);

        String viewName = paymentController.getAsyncWalletResult(requestId, session, model, redirectAttributes);

        assertEquals("payment/Wallet", viewName);
        verify(model).addAttribute(eq("penyewaEmails"), any(Map.class));
        verify(authService).findById(testUserId);
    }

    @Test
    void getAsyncWalletResult_whenExceptionOccurs_shouldRedirectWithError() {
        String requestId = "test-request-id";
        CompletableFuture<List<Payment>> transactionsFuture = new CompletableFuture<>();
        transactionsFuture.completeExceptionally(new RuntimeException("Database error"));
        CompletableFuture<BigDecimal> balanceFuture = CompletableFuture.completedFuture(balance);

        when(session.getAttribute("transaction_request_" + requestId)).thenReturn(transactionsFuture);
        when(session.getAttribute("balance_request_" + requestId)).thenReturn(balanceFuture);
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);

        String viewName = paymentController.getAsyncWalletResult(requestId, session, model, redirectAttributes);

        assertEquals("redirect:/payment/wallet", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Terjadi kesalahan"));
    }
}
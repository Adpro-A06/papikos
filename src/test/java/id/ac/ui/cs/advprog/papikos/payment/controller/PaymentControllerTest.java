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
import java.util.UUID;

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
    void topUp_whenUserNotPenyewa_shouldRedirectToHome() {
        // Test for non-PENYEWA role attempting top-up
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.topUp(pemilikKosId, amount, session, redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
        verify(paymentService, never()).topUp(any(), any());
    }

    @Test
    void showPaymentForm_whenUserNotPenyewa_shouldRedirectToHome() {
        // Test for non-PENYEWA role accessing payment form
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        String viewName = paymentController.showPaymentForm(null, null, null, session, model, redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
    }

    @Test
    void showPaymentForm_whenPemilikNotFound_shouldHandleException() {
        // Test exception handling when finding pemilik
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);
        when(authService.findAllPemilikKos()).thenReturn(pemilikKosList);
        when(authService.findById(pemilikKosId)).thenThrow(new RuntimeException("User not found"));

        String viewName = paymentController.showPaymentForm(pemilikKosId, null, null, session, model, redirectAttributes);

        assertEquals("payment/PaymentForm", viewName);
        // Verify that the exception was caught and handled gracefully
        verify(model, never()).addAttribute(eq("pemilikEmail"), any());
    }

    @Test
    void pay_whenUserNotPenyewa_shouldRedirectToHome() {
        // Test for non-PENYEWA role attempting payment
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(pemilikKosId.toString());
        when(authService.findById(pemilikKosId)).thenReturn(pemilikKosUser);

        BigDecimal amount = new BigDecimal("50000");

        String viewName = paymentController.pay(testUserId, pemilikKosId, amount, session, redirectAttributes);

        assertEquals("redirect:/", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
        verify(paymentService, never()).pay(any(), any(), any());
    }

    @Test
    void showWallet_whenUserNotLoggedIn_shouldRedirectToLogin() {
        // Test wallet access without authentication
        when(session.getAttribute("JWT_TOKEN")).thenReturn(null);

        String viewName = paymentController.showWallet(null, null, null, session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }

    @Test
    void showWallet_withInvalidTransactionType_shouldIgnoreType() {
        // Test with invalid transaction type string
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);

        List<Payment> transactions = new ArrayList<>();
        when(paymentService.filterTransactions(eq(testUserId), isNull(), isNull(), isNull()))
                .thenReturn(transactions);

        String invalidType = "INVALID_TYPE";
        String viewName = paymentController.showWallet(null, null, invalidType, session, model, redirectAttributes);

        assertEquals("payment/Wallet", viewName);
        // Should call filterTransactions with null type since invalid type was provided
        verify(paymentService).filterTransactions(testUserId, null, null, null);
    }



    @Test
    void getCurrentUser_withInvalidToken_shouldReturnNull() {
        // Test token decoding failure
        when(session.getAttribute("JWT_TOKEN")).thenReturn("invalid-token");
        when(authService.decodeToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        String viewName = paymentController.showTopUpForm(session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }

    @Test
    void getCurrentUser_withInvalidUUID_shouldReturnNull() {
        // Test invalid UUID in token
        when(session.getAttribute("JWT_TOKEN")).thenReturn("valid-token");
        when(authService.decodeToken("valid-token")).thenReturn("invalid-uuid");

        String viewName = paymentController.showTopUpForm(session, model, redirectAttributes);

        assertEquals("redirect:/api/auth/login", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "Silakan login terlebih dahulu");
    }
}
package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.authentication.dto.AuthDto;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.payment.dto.PaymentDto;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRestControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PaymentRestController paymentRestController;

    private User testUser;
    private User pemilikUser;
    private UUID testUserId;
    private UUID pemilikUserId;
    private String validAuthHeader;
    private String invalidAuthHeader;
    private final BigDecimal amount = new BigDecimal("100000");

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        pemilikUserId = UUID.randomUUID();

        testUser = new User("test@example.com", "Password123!", Role.PENYEWA);

        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testUser, testUserId);
        } catch (Exception e) {

        }

        pemilikUser = new User("pemilik@example.com", "Password123!", Role.PEMILIK_KOS);

        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pemilikUser, pemilikUserId);
        } catch (Exception e) {

        }

        validAuthHeader = "Bearer valid-token";
        invalidAuthHeader = "Bearer invalid-token";
    }

    @Test
    void topUp_HappyPath_ShouldReturnSuccessResponse() {

        PaymentDto.TopUpRequest request = new PaymentDto.TopUpRequest();
        request.setUserId(testUserId);
        request.setAmount(amount);

        BigDecimal newBalance = new BigDecimal("150000");

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(newBalance);
        doNothing().when(paymentService).topUp(testUserId, amount);

        ResponseEntity<?> response = paymentRestController.topUp(request, validAuthHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Top-up berhasil", apiResponse.getMessage());

        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        assertEquals(testUserId, data.get("userId"));
        assertEquals(amount, data.get("amountAdded"));
        assertEquals(newBalance, data.get("newBalance"));

        verify(paymentService).topUp(testUserId, amount);
        verify(paymentService).getBalance(testUserId);
    }

    @Test
    void topUp_UnhappyPath_InvalidToken_ShouldReturnUnauthorized() {

        PaymentDto.TopUpRequest request = new PaymentDto.TopUpRequest();
        request.setUserId(testUserId);
        request.setAmount(amount);

        when(authService.decodeToken(anyString())).thenThrow(new RuntimeException("Invalid token"));


        ResponseEntity<?> response = paymentRestController.topUp(request, invalidAuthHeader);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Token tidak valid", apiResponse.getMessage());

        verify(paymentService, never()).topUp(any(UUID.class), any(BigDecimal.class));
    }

    @Test
    void topUp_UnhappyPath_DifferentUser_ShouldReturnForbidden() {

        PaymentDto.TopUpRequest request = new PaymentDto.TopUpRequest();
        request.setUserId(UUID.randomUUID());
        request.setAmount(amount);

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);


        ResponseEntity<?> response = paymentRestController.topUp(request, validAuthHeader);


        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Anda hanya dapat top-up ke akun Anda sendiri", apiResponse.getMessage());

        verify(paymentService, never()).topUp(any(UUID.class), any(BigDecimal.class));
    }

    @Test
    void topUp_UnhappyPath_ServiceThrowsException_ShouldReturnBadRequest() {

        PaymentDto.TopUpRequest request = new PaymentDto.TopUpRequest();
        request.setUserId(testUserId);
        request.setAmount(new BigDecimal("-100"));

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        doThrow(new IllegalArgumentException("Jumlah top-up tidak boleh kurang dari 0")).when(paymentService).topUp(testUserId, request.getAmount());


        ResponseEntity<?> response = paymentRestController.topUp(request, validAuthHeader);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Jumlah top-up tidak boleh kurang dari 0", apiResponse.getMessage());
    }



    @Test
    void pay_HappyPath_ShouldReturnSuccessResponse() {

        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setFromUserId(testUserId);
        request.setToUserId(pemilikUserId);
        request.setAmount(amount);

        BigDecimal newBalance = new BigDecimal("50000");

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(newBalance);
        doNothing().when(paymentService).pay(testUserId, pemilikUserId, amount);


        ResponseEntity<?> response = paymentRestController.pay(request, validAuthHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Pembayaran berhasil", apiResponse.getMessage());

        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        assertEquals(testUserId, data.get("fromUserId"));
        assertEquals(pemilikUserId, data.get("toUserId"));
        assertEquals(amount, data.get("amountPaid"));
        assertEquals(newBalance, data.get("newBalance"));

        verify(paymentService).pay(testUserId, pemilikUserId, amount);
        verify(paymentService).getBalance(testUserId);
    }

    @Test
    void pay_UnhappyPath_InvalidToken_ShouldReturnUnauthorized() {

        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setFromUserId(testUserId);
        request.setToUserId(pemilikUserId);
        request.setAmount(amount);

        when(authService.decodeToken(anyString())).thenThrow(new RuntimeException("Invalid token"));


        ResponseEntity<?> response = paymentRestController.pay(request, invalidAuthHeader);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Token tidak valid", apiResponse.getMessage());

        verify(paymentService, never()).pay(any(UUID.class), any(UUID.class), any(BigDecimal.class));
    }

    @Test
    void pay_UnhappyPath_DifferentFromUser_ShouldReturnForbidden() {

        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setFromUserId(UUID.randomUUID());
        request.setToUserId(pemilikUserId);
        request.setAmount(amount);

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);


        ResponseEntity<?> response = paymentRestController.pay(request, validAuthHeader);


        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Anda hanya dapat melakukan pembayaran dari akun Anda sendiri", apiResponse.getMessage());

        verify(paymentService, never()).pay(any(UUID.class), any(UUID.class), any(BigDecimal.class));
    }

    @Test
    void pay_UnhappyPath_InsufficientBalance_ShouldReturnBadRequest() {

        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setFromUserId(testUserId);
        request.setToUserId(pemilikUserId);
        request.setAmount(amount);

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        doThrow(new IllegalArgumentException("Saldo tidak mencukupi")).when(paymentService).pay(testUserId, pemilikUserId, amount);


        ResponseEntity<?> response = paymentRestController.pay(request, validAuthHeader);


        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Saldo tidak mencukupi", apiResponse.getMessage());
    }


    @Test
    void getBalance_HappyPath_ShouldReturnBalance() {
        BigDecimal balance = new BigDecimal("150000");

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.getBalance(testUserId)).thenReturn(balance);


        ResponseEntity<?> response = paymentRestController.getBalance(validAuthHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Saldo berhasil diambil", apiResponse.getMessage());

        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        assertEquals(testUserId, data.get("userId"));
        assertEquals(balance, data.get("balance"));

        verify(paymentService).getBalance(testUserId);
    }

    @Test
    void getBalance_UnhappyPath_InvalidToken_ShouldReturnUnauthorized() {

        when(authService.decodeToken(anyString())).thenThrow(new RuntimeException("Invalid token"));


        ResponseEntity<?> response = paymentRestController.getBalance(invalidAuthHeader);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Token tidak valid", apiResponse.getMessage());

        verify(paymentService, never()).getBalance(any(UUID.class));
    }



    @Test
    void getTransactions_HappyPath_WithFilters_ShouldReturnTransactions() {

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        String transactionType = TransactionType.PAYMENT.name();

        List<Payment> transactions = new ArrayList<>();
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setFromUserId(testUserId);
        payment.setToUserId(pemilikUserId);
        payment.setAmount(amount);
        payment.setType(TransactionType.PAYMENT);
        payment.setTimestamp(LocalDateTime.now());
        transactions.add(payment);

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.filterTransactions(eq(testUserId), eq(startDate), eq(endDate), eq(TransactionType.PAYMENT)))
            .thenReturn(transactions);


        ResponseEntity<?> response = paymentRestController.getTransactions(startDate, endDate, transactionType, validAuthHeader);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());
        assertEquals("Transaksi berhasil diambil", apiResponse.getMessage());

        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        assertEquals(testUserId, data.get("userId"));
        assertEquals(transactions, data.get("transactions"));

        verify(paymentService).filterTransactions(testUserId, startDate, endDate, TransactionType.PAYMENT);
    }

    @Test
    void getTransactions_HappyPath_NoFilters_ShouldReturnAllTransactions() {

        List<Payment> transactions = new ArrayList<>();

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.filterTransactions(eq(testUserId), isNull(), isNull(), isNull()))
            .thenReturn(transactions);


        ResponseEntity<?> response = paymentRestController.getTransactions(null, null, null, validAuthHeader);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.isSuccess());

        verify(paymentService).filterTransactions(testUserId, null, null, null);
    }

    @Test
    void getTransactions_UnhappyPath_InvalidToken_ShouldReturnUnauthorized() {

        when(authService.decodeToken(anyString())).thenThrow(new RuntimeException("Invalid token"));


        ResponseEntity<?> response = paymentRestController.getTransactions(null, null, null, invalidAuthHeader);


        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Token tidak valid", apiResponse.getMessage());

        verify(paymentService, never()).filterTransactions(any(), any(), any(), any());
    }

    @Test
    void getTransactions_UnhappyPath_InvalidTransactionType_ShouldUseNullType() {

        String invalidTransactionType = "INVALID_TYPE";
        List<Payment> transactions = new ArrayList<>();

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.filterTransactions(eq(testUserId), isNull(), isNull(), isNull()))
            .thenReturn(transactions);

        ResponseEntity<?> response = paymentRestController.getTransactions(null, null, invalidTransactionType, validAuthHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(paymentService).filterTransactions(testUserId, null, null, null);
    }

    @Test
    void getTransactions_UnhappyPath_ServiceException_ShouldReturnBadRequest() {

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(14);

        when(authService.decodeToken(anyString())).thenReturn(testUserId.toString());
        when(authService.findById(testUserId)).thenReturn(testUser);
        when(paymentService.filterTransactions(eq(testUserId), eq(startDate), eq(endDate), isNull()))
            .thenThrow(new IllegalArgumentException("Tanggal akhir tidak boleh sebelum tanggal awal"));

        ResponseEntity<?> response = paymentRestController.getTransactions(startDate, endDate, null, validAuthHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthDto.ApiResponse apiResponse = (AuthDto.ApiResponse) response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.isSuccess());
        assertEquals("Tanggal akhir tidak boleh sebelum tanggal awal", apiResponse.getMessage());
    }
}

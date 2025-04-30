package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Model model;

    @InjectMocks
    private PaymentController paymentController;

    private String userId;
    private String toUserId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        toUserId = UUID.randomUUID().toString();
    }

    @Test
    void topUp_success() {
        BigDecimal amount = new BigDecimal("50000");

        String view = paymentController.topUp(userId, amount, model);

        verify(paymentService).topUp(UUID.fromString(userId), amount);
        assertEquals("redirect:/payment/list", view);
    }

    @Test
    void pay_success() {
        BigDecimal amount = new BigDecimal("100000");

        String view = paymentController.pay(userId, toUserId, amount, model);

        verify(paymentService).pay(UUID.fromString(userId), UUID.fromString(toUserId), amount);
        assertEquals("redirect:/payment/list", view);
    }

    @Test
    void list_shouldReturnViewName() {
        String view = paymentController.paymentListPage(model);
        assertEquals("PaymentList", view);
    }
}

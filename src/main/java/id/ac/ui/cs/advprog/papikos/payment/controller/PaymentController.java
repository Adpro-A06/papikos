package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public String paymentMenuPage() {
        return "payment/payment";
    }

    @GetMapping("/topup/form")
    public String topUpForm() {
        return "payment/topup";
    }

    @GetMapping("/pay/form")
    public String payForm() {
        return "payment/pay";
    }

    @PostMapping("/topup")
    public String topUp(@RequestParam String userId,
                        @RequestParam BigDecimal amount,
                        Model model) {
        paymentService.topUp(UUID.fromString(userId), amount);
        return "redirect:/payment/list";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam String fromUserId,
                      @RequestParam String toUserId,
                      @RequestParam BigDecimal amount,
                      Model model) {
        paymentService.pay(UUID.fromString(fromUserId), UUID.fromString(toUserId), amount);
        return "redirect:/payment/list";
    }

    @GetMapping("/list")
    public String paymentListPage(Model model) {
        // TODO: model.addAttribute("transactions", paymentService.getAllTransactions());
        return "payment/PaymentList";
    }
}

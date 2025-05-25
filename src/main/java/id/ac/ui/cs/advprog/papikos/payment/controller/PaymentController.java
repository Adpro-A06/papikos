package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.monitoring.PaymentMetrics;
import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final AuthService authService;
    private final PaymentMetrics paymentMetrics;

    @GetMapping("/topup")
    public String showTopUpForm(HttpSession session, Model model, RedirectAttributes ra) {
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to access top-up form");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            log.warn("Non-renter user (role: {}) attempted to access top-up form", user.getRole());
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
            return "redirect:/";
        }

        BigDecimal balance = paymentService.getBalance(user.getId());
        model.addAttribute("balance", balance);
        model.addAttribute("userId", user.getId());
        model.addAttribute("user", user);
        log.debug("Showing top-up form for user {} with balance {}", user.getId(), balance);

        return "payment/TopUp";
    }

    @PostMapping("/topup")
    public String topUp(
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount,
            HttpSession session,
            RedirectAttributes ra) {

        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to perform top-up operation");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            log.warn("Non-renter user (role: {}) attempted to perform top-up operation", user.getRole());
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
            return "redirect:/";
        }

        if (!user.getId().equals(userId)) {
            log.warn("User {} attempted to top-up for a different user account {}", user.getId(), userId);
            ra.addFlashAttribute("error", "Anda hanya dapat top-up ke akun Anda sendiri");
            return "redirect:/payment/topup";
        }

        try {
            log.info("User {} initiated top-up for amount {}", userId, amount);
            paymentService.topUp(userId, amount);
            log.info("Top-up successful for user {} with amount {}", userId, amount);
            ra.addFlashAttribute("success", "Top-up berhasil. Rp " + amount + " telah ditambahkan ke akun Anda.");
        } catch (IllegalArgumentException e) {
            log.error("Top-up failed for user {} with amount {}: {}", userId, amount, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payment/wallet";
    }

    @GetMapping("/pay")
    public String showPaymentForm(
            @RequestParam(required = false) UUID toUserId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String description,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to access payment form");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            log.warn("Non-renter user (role: {}) attempted to access payment form", user.getRole());
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
            return "redirect:/";
        }

        BigDecimal balance = paymentService.getBalance(user.getId());
        model.addAttribute("balance", balance);
        model.addAttribute("fromUserId", user.getId());
        model.addAttribute("user", user);

        List<User> allPemilikKos = authService.findAllPemilikKos();
        model.addAttribute("allPemilikKos", allPemilikKos);

        if (toUserId != null) {
            model.addAttribute("toUserId", toUserId);

            try {
                User pemilik = authService.findById(toUserId);
                model.addAttribute("pemilikEmail", pemilik.getEmail());
            } catch (Exception e) {
                log.error("Failed to find pemilik with ID {}: {}", toUserId, e.getMessage());
            }
        }

        if (amount != null) {
            model.addAttribute("prefilledAmount", amount);
        }

        if (description != null) {
            model.addAttribute("prefilledDescription", description);
        }

        log.debug("Showing payment form for user {} with balance {}", user.getId(), balance);
        return "payment/PaymentForm";
    }

    @PostMapping("/pay")
    public String pay(
            @RequestParam UUID fromUserId,
            @RequestParam UUID toUserId,
            @RequestParam BigDecimal amount,
            HttpSession session,
            RedirectAttributes ra) {

        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to perform payment operation");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            log.warn("Non-renter user (role: {}) attempted to perform payment operation", user.getRole());
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
            return "redirect:/";
        }

        if (!user.getId().equals(fromUserId)) {
            log.warn("User {} attempted to pay from a different user account {}", user.getId(), fromUserId);
            ra.addFlashAttribute("error", "Anda hanya dapat melakukan pembayaran dari akun Anda sendiri");
            return "redirect:/payment/wallet";
        }

        try {
            log.info("User {} initiated payment to user {} for amount {}", fromUserId, toUserId, amount);
            paymentService.pay(fromUserId, toUserId, amount);
            log.info("Payment successful from user {} to user {} for amount {}", fromUserId, toUserId, amount);
            ra.addFlashAttribute("success", "Pembayaran berhasil. Rp " + amount + " telah dikirim.");
        } catch (IllegalArgumentException e) {
            log.error("Payment failed from user {} to user {} for amount {}: {}", fromUserId, toUserId, amount, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payment/wallet";
    }

    @GetMapping("/wallet")
    public String showWallet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String transactionType,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("Unauthenticated user attempted to access wallet");
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        TransactionType type = null;
        if (transactionType != null && !transactionType.isEmpty()) {
            try {
                type = TransactionType.valueOf(transactionType);
            } catch (IllegalArgumentException ignored) {
                log.warn("Invalid transaction type provided: {}", transactionType);
            }
        }

        List<Payment> transactions = paymentService.filterTransactions(
                user.getId(), startDate, endDate, type);

        if (user.getRole() == Role.PEMILIK_KOS) {
            java.util.Map<UUID, String> penyewaEmails = new java.util.HashMap<>();

            for (Payment payment : transactions) {
                if (payment.getType() == TransactionType.PAYMENT && payment.getFromUserId() != null) {
                    try {
                        User penyewa = authService.findById(payment.getFromUserId());
                        if (penyewa != null) {
                            penyewaEmails.put(payment.getFromUserId(), penyewa.getEmail());
                        }
                    } catch (Exception ignored) {
                        log.warn("Failed to find penyewa with ID {}", payment.getFromUserId());
                    }
                }
            }

            model.addAttribute("penyewaEmails", penyewaEmails);
        }

        BigDecimal balance = paymentService.getBalance(user.getId());

        model.addAttribute("transactions", transactions);
        model.addAttribute("balance", balance);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("transactionType", transactionType);
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("wallet", new Object() {
            public BigDecimal getBalance() { return balance; }
        });
        model.addAttribute("recentTransactions", transactions);
        model.addAttribute("user", user);

        log.debug("Showing wallet for user {} with balance {}", user.getId(), balance);
        return "payment/Wallet";
    }

    private User getCurrentUser(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            log.warn("No JWT token found in session");
            return null;
        }

        try {
            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
        } catch (Exception e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            return null;
        }
    }
}


package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

    private final PaymentService paymentService;
    private final AuthService authService;

    @GetMapping("/topup")
    public String showTopUpForm(HttpSession session, Model model, RedirectAttributes ra) {
        User user = getCurrentUser(session);
        if (user == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
            return "redirect:/";
        }

        BigDecimal balance = paymentService.getBalance(user.getId());
        model.addAttribute("balance", balance);
        model.addAttribute("userId", user.getId());
        model.addAttribute("user", user);

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
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan top-up");
            return "redirect:/";
        }

        if (!user.getId().equals(userId)) {
            ra.addFlashAttribute("error", "Anda hanya dapat top-up ke akun Anda sendiri");
            return "redirect:/payment/topup";
        }

        try {
            paymentService.topUp(userId, amount);
            ra.addFlashAttribute("success", "Top-up berhasil. Rp " + amount + " telah ditambahkan ke akun Anda.");
        } catch (IllegalArgumentException e) {
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
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
            return "redirect:/";
        }

        BigDecimal balance = paymentService.getBalance(user.getId());
        model.addAttribute("balance", balance);
        model.addAttribute("fromUserId", user.getId());
        model.addAttribute("user", user);

        // Get all pemilik kos (landlords) for dropdown selection
        List<User> allPemilikKos = authService.findAllPemilikKos();
        model.addAttribute("allPemilikKos", allPemilikKos);

        // Pre-fill data if provided
        if (toUserId != null) {
            model.addAttribute("toUserId", toUserId);
            // Get pemilik info for display
            try {
                User pemilik = authService.findById(toUserId);
                model.addAttribute("pemilikEmail", pemilik.getEmail());
            } catch (Exception e) {
                // Handle error silently, will show generic message
            }
        }

        if (amount != null) {
            model.addAttribute("prefilledAmount", amount);
        }

        if (description != null) {
            model.addAttribute("prefilledDescription", description);
        }

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
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Hanya penyewa yang dapat melakukan pembayaran");
            return "redirect:/";
        }

        if (!user.getId().equals(fromUserId)) {
            ra.addFlashAttribute("error", "Anda hanya dapat melakukan pembayaran dari akun Anda sendiri");
            return "redirect:/payment/wallet";
        }

        try {
            paymentService.pay(fromUserId, toUserId, amount);
            ra.addFlashAttribute("success", "Pembayaran berhasil. Rp " + amount + " telah dikirim.");
        } catch (IllegalArgumentException e) {
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
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        TransactionType type = null;
        if (transactionType != null && !transactionType.isEmpty()) {
            try {
                type = TransactionType.valueOf(transactionType);
            } catch (IllegalArgumentException ignored) {
            }
        }

        List<Payment> transactions = paymentService.filterTransactions(
                user.getId(), startDate, endDate, type);

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

        return "payment/Wallet";
    }

    private User getCurrentUser(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            return null;
        }

        try {
            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
        } catch (Exception e) {
            return null;
        }
    }
}


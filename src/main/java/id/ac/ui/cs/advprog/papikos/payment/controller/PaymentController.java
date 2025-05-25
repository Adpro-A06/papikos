package id.ac.ui.cs.advprog.papikos.payment.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.payment.model.Payment;
import id.ac.ui.cs.advprog.papikos.payment.model.TransactionType;
import id.ac.ui.cs.advprog.papikos.payment.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
@Slf4j
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

    @PostMapping("/topup-async")
    public String topUpAsync(
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
            paymentService.topUpAsync(userId, amount)
                .thenRun(() -> log.info("Top-up asinkron selesai untuk user: {}, jumlah: {}", userId, amount))
                .exceptionally(ex -> {
                    log.error("Error pada top-up asinkron untuk user: {}", userId, ex);
                    return null;
                });

            ra.addFlashAttribute("info", "Permintaan top-up sedang diproses. Rp " + amount + " akan segera ditambahkan ke akun Anda.");
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

        List<User> allPemilikKos = authService.findAllPemilikKos();
        model.addAttribute("allPemilikKos", allPemilikKos);

        if (toUserId != null) {
            model.addAttribute("toUserId", toUserId);

            try {
                User pemilik = authService.findById(toUserId);
                model.addAttribute("pemilikEmail", pemilik.getEmail());
            } catch (Exception e) {

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

    @PostMapping("/pay-async")
    public String payAsync(
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
            paymentService.payAsync(fromUserId, toUserId, amount)
                .thenRun(() -> log.info("Pembayaran asinkron selesai dari user: {} ke user: {}, jumlah: {}",
                    fromUserId, toUserId, amount))
                .exceptionally(ex -> {
                    log.error("Error pada pembayaran asinkron dari user: {} ke user: {}",
                        fromUserId, toUserId, ex);
                    return null;
                });
            ra.addFlashAttribute("info", "Permintaan pembayaran sedang diproses. Rp " + amount + " akan segera dikirim.");
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

        return "payment/Wallet";
    }

    @GetMapping("/wallet-async")
    public String showWalletAsync(
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

        model.addAttribute("user", user);
        model.addAttribute("loadingTransactions", true);
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("transactionType", transactionType);

        TransactionType type = null;
        if (transactionType != null && !transactionType.isEmpty()) {
            try {
                type = TransactionType.valueOf(transactionType);
            } catch (IllegalArgumentException ignored) {
            }
        }

        final TransactionType finalType = type;
        CompletableFuture<List<Payment>> transactionsFuture =
            paymentService.filterTransactionsAsync(user.getId(), startDate, endDate, finalType);

        CompletableFuture<BigDecimal> balanceFuture =
            paymentService.getBalanceAsync(user.getId());

        String requestId = UUID.randomUUID().toString();
        session.setAttribute("transaction_request_" + requestId, transactionsFuture);
        session.setAttribute("balance_request_" + requestId, balanceFuture);

        model.addAttribute("requestId", requestId);

        return "payment/WalletLoading";
    }

    @GetMapping("/wallet-async/status/{requestId}")
    @ResponseBody
    public String checkAsyncWalletStatus(@PathVariable String requestId, HttpSession session) {
        CompletableFuture<List<Payment>> transactionsFuture =
            (CompletableFuture<List<Payment>>) session.getAttribute("transaction_request_" + requestId);

        CompletableFuture<BigDecimal> balanceFuture =
            (CompletableFuture<BigDecimal>) session.getAttribute("balance_request_" + requestId);

        if (transactionsFuture == null || balanceFuture == null) {
            return "error";
        }

        if (transactionsFuture.isDone() && balanceFuture.isDone()) {
            return "complete";
        }

        return "processing";
    }

    @GetMapping("/wallet-async/result/{requestId}")
    public String getAsyncWalletResult(
            @PathVariable String requestId,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {

        CompletableFuture<List<Payment>> transactionsFuture =
            (CompletableFuture<List<Payment>>) session.getAttribute("transaction_request_" + requestId);

        CompletableFuture<BigDecimal> balanceFuture =
            (CompletableFuture<BigDecimal>) session.getAttribute("balance_request_" + requestId);

        if (transactionsFuture == null || balanceFuture == null) {
            ra.addFlashAttribute("error", "Permintaan tidak ditemukan atau sudah kedaluwarsa");
            return "redirect:/payment/wallet";
        }

        try {
            User user = getCurrentUser(session);
            List<Payment> transactions = transactionsFuture.get();
            BigDecimal balance = balanceFuture.get();

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
                        }
                    }
                }

                model.addAttribute("penyewaEmails", penyewaEmails);
            }

            model.addAttribute("transactions", transactions);
            model.addAttribute("balance", balance);
            model.addAttribute("transactionTypes", TransactionType.values());
            model.addAttribute("wallet", new Object() {
                public BigDecimal getBalance() { return balance; }
            });
            model.addAttribute("recentTransactions", transactions);
            model.addAttribute("user", user);
            model.addAttribute("asyncLoaded", true);

            session.removeAttribute("transaction_request_" + requestId);
            session.removeAttribute("balance_request_" + requestId);

            return "payment/Wallet";

        } catch (Exception e) {
            log.error("Error getting async wallet results", e);
            ra.addFlashAttribute("error", "Terjadi kesalahan saat memproses data: " + e.getMessage());
            return "redirect:/payment/wallet";
        }
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


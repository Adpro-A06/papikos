package id.ac.ui.cs.advprog.papikos.authentication.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminApprovalController {

    private final AuthService authService;

    @Autowired
    public AdminApprovalController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/pending-approvals")
    public String pendingApprovals(HttpSession session, Model model, RedirectAttributes ra) {
        User admin = getUserFromSession(session);
        if (admin == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (admin.getRole() != Role.ADMIN) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        List<User> pendingOwners = authService.findAllPendingPemilikKos();
        model.addAttribute("pendingOwners", pendingOwners);
        return "admin/PendingApprovals";
    }

    @PostMapping("/approve")
    public String approvePemilikKos(
            @RequestParam("userId") String userIdStr,
            HttpSession session,
            RedirectAttributes ra
    ) {
        User admin = getUserFromSession(session);
        if (admin == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/api/auth/login";
        }

        if (admin.getRole() != Role.ADMIN) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses untuk melakukan tindakan ini");
            return "redirect:/api/auth/login";
        }

        try {
            UUID userId = UUID.fromString(userIdStr);
            authService.approvePemilikKos(userId);
            ra.addFlashAttribute("success", "Pemilik kos berhasil disetujui");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Format ID tidak valid");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/pending-approvals";
    }

    private User getUserFromSession(HttpSession session) {
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
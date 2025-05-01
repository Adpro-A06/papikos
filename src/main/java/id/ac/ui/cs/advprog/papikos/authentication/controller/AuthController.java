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

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "authentication/RegisterPage";
    }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes ra
    ) {
        try {
            Role r = Role.valueOf(role);
            authService.registerUser(email, password, r);
            ra.addFlashAttribute("success", "Registrasi berhasil! Silakan login.");
            return "redirect:/api/auth/login";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/api/auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model m) {
        return "authentication/LoginPage";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra
    ) {
        try {
            String token = authService.login(email, password);
            session.setAttribute("JWT_TOKEN", token);

            String idStr = authService.decodeToken(token);
            User user = authService.findById(java.util.UUID.fromString(idStr));

            if (user.getRole() == Role.PENYEWA) {
                return "redirect:/penyewa/home";
            } else if (user.getRole() == Role.PEMILIK_KOS) {
                return "redirect:/pemilik/home";
            } else {
                return "redirect:/admin/home";
            }
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/api/auth/login";
        }
    }

    @PostMapping("/logout")
    public String doLogout(HttpSession session, RedirectAttributes ra) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token != null) {
            try {
                authService.logout(token);
            } catch (RuntimeException ignored) {
                // Logout errors are ignored for now
            }
            session.removeAttribute("JWT_TOKEN");
        }
        ra.addFlashAttribute("success", "Anda telah logout.");
        return "redirect:/api/auth/login";
    }
}
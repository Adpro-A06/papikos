package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
public class KosController {

    private final KosService kosService;
    private final AuthService authService;

    @Autowired
    public KosController(KosService kosService, AuthService authService) {
        this.kosService = kosService;
        this.authService = authService;
    }

    @GetMapping("/kos/search")
    public String searchKos(@RequestParam(required = false) String keyword,
                            HttpSession session,
                            Model model,
                            RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        List<Kos> searchResults;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = kosService.searchByKeyword(keyword);
        } else {
            searchResults = kosService.findAllAvailable();
        }

        model.addAttribute("kosList", searchResults);
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);
        return "home/PenyewaHome";
    }

    @GetMapping("/kos/{id}")
    public String viewKosDetail(@PathVariable String id,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        try {
            UUID kosId = UUID.fromString(id);
            Kos kos = kosService.findById(kosId)
                    .orElseThrow(() -> new EntityNotFoundException("Kos tidak ditemukan"));

            model.addAttribute("kos", kos);
            model.addAttribute("user", user);
            return "penyewaan/DetailKos";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Format ID kos tidak valid");
            return "redirect:/penyewa/home";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewa/home";
        }
    }

    private User getCurrentUser(HttpSession session, RedirectAttributes ra) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        if (token == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return null;
        }

        try {
            String idStr = authService.decodeToken(token);
            return authService.findById(UUID.fromString(idStr));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Sesi login Anda telah berakhir. Silakan login kembali.");
            session.removeAttribute("JWT_TOKEN");
            return null;
        }
    }
}
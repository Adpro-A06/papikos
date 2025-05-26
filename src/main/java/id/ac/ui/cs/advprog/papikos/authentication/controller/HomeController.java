package id.ac.ui.cs.advprog.papikos.authentication.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    private final AuthService authService;
    private final KosService kosService;
    private final WishlistService wishlistService;

    @Autowired
    public HomeController(AuthService authService, KosService kosService,  WishlistService wishlistService) {
        this.authService = authService;
        this.kosService = kosService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/api/auth/login";
    }

    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.ADMIN) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        return "home/AdminHome";
    }

    @GetMapping("/pemilik/home")
    public String pemilikKosHome(HttpSession session, RedirectAttributes ra, Model model) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        model.addAttribute("approved", user.isApproved());
        model.addAttribute("user", user);

        return "home/PemilikKosHome";
    }

    @GetMapping("/penyewa/home")
    public String penyewaHome(HttpSession session, Model model, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }
        List<Kos> availableKosList = kosService.findAllAvailable();
        model.addAttribute("kosList", availableKosList);
        model.addAttribute("user", user);

        List<Long> userWishlist = wishlistService.getUserWishlistKosIdsAsLong(user.getId());
        if (userWishlist == null) {
            userWishlist = new ArrayList<>();
        }
        model.addAttribute("userWishlist", userWishlist);
        return "home/PenyewaHome";
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
            return null;
        }
    }
}
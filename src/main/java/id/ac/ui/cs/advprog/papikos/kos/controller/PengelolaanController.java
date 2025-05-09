package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.service.PengelolaanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/pemilik")
public class PengelolaanController {
    private final PengelolaanService service;
    private final AuthService authService;

    @Autowired
    public PengelolaanController(PengelolaanService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping("/create")
    public String createKosPage(Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        Kos kos = new Kos();
        model.addAttribute("kos", kos);
        return "pengelolaan/CreateKos";
    }

    @GetMapping("/daftarkos")
    public String kosListPage(HttpSession session, RedirectAttributes ra, Model model) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        List<Kos> allKos = service.findAll();
        for (Kos kos : allKos) {
            System.out.println("DEBUG: ID Kos = " + kos.getId());  // Debug ID di controller
        }
        model.addAttribute("allKos", allKos);
        return "pengelolaan/ListKos";
    }

    @PostMapping("/create")
    public String createKosPost(@ModelAttribute Kos kos, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        if (kos.getId() == null || kos.getId().isEmpty()) {
            kos.setId(UUID.randomUUID().toString());
        }
        service.create(kos);
        return "redirect:daftarkos";
    }

    @GetMapping("/edit/{id}")
    public String editKosPage(@PathVariable String id, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        try {
            Kos kos = service.findById(id);
            model.addAttribute("kos", kos);
            return "pengelolaan/EditKos";
        }
        catch (PengelolaanRepository.KosNotFoundException e) {
            System.out.println("kos not found");
            return "pengelolaan/error/KosNotFound";
        }
    }

    @PostMapping("/edit/{id}")
    public String editKosPost(@PathVariable String id, @ModelAttribute Kos kos, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        try {
            service.update(kos);
            return "redirect:/pemilik/daftarkos";
        }
        catch (PengelolaanRepository.KosNotFoundException e) {
            return "pengelolaan/error/KosNotFound";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteKos(@PathVariable String id, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        try {
            Kos kos = service.findById(id);
            service.delete(kos);
            return "redirect:/pemilik/daftarkos";
        } catch (PengelolaanRepository.KosNotFoundException e) {
            model.addAttribute("errorMessage", "Kos dengan ID " + id + " tidak ditemukan.");
            return "pengelolaan/error/KosNotFound";
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
            return null;
        }
    }
}
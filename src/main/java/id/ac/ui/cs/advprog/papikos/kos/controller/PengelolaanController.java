package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.kos.service.PengelolaanService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
        List<Kos> filteredKos = allKos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        model.addAttribute("allKos", filteredKos);
        return "pengelolaan/ListKos";
    }

    @PostMapping("/create")
    public String createKosPost(@Valid @ModelAttribute Kos kos, BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("kos", kos);
            return "pengelolaan/CreateKos";
        }

        try {
            kos.setPemilik(user);
            service.create(kos);
            ra.addFlashAttribute("success", "Kos berhasil dibuat");
            return "redirect:daftarkos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal membuat kos: " + e.getMessage());
            return "redirect:/pemilik/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String editKosPage(@PathVariable UUID id, Model model, HttpSession session, RedirectAttributes ra) {
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
        } catch (PengelolaanRepository.KosNotFoundException e) {
            System.out.println("kos not found");
            return "pengelolaan/error/KosNotFound";
        }
    }

    @PostMapping("/edit/{id}")
    public String editKosPost(@PathVariable UUID id, @Valid @ModelAttribute Kos kos, BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/api/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("kos", kos);
            return "pengelolaan/EditKos";
        }

        try {
            kos.setId(id);
            kos.setPemilik(user);
            service.update(kos);
            ra.addFlashAttribute("success", "Kos berhasil diperbarui");
            return "redirect:/pemilik/daftarkos";
        } catch (PengelolaanRepository.KosNotFoundException e) {
            return "pengelolaan/error/KosNotFound";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Gagal memperbarui kos: " + e.getMessage());
            return "redirect:/pemilik/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteKos(@PathVariable UUID id, Model model, HttpSession session, RedirectAttributes ra) {
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
            ra.addFlashAttribute("success", "Kos berhasil dihapus");
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
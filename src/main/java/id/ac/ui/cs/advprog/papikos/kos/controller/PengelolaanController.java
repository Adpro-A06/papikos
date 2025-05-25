package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public CompletableFuture<String> createKosPage(Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        Kos kos = new Kos();
        model.addAttribute("kos", kos);
        return CompletableFuture.completedFuture("pengelolaan/CreateKos");
    }

    @GetMapping("/daftarkos")
    public CompletableFuture<String> kosListPage(HttpSession session, RedirectAttributes ra, Model model) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        return service.findAll()
                .thenApply(allKos -> {
                    List<Kos> filteredKos = allKos.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    model.addAttribute("allKos", filteredKos);
                    return "pengelolaan/ListKos";
                });
    }

    @PostMapping("/create")
    public CompletableFuture<String> createKosPost(@Valid @ModelAttribute Kos kos, BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("kos", kos);
            return CompletableFuture.completedFuture("pengelolaan/CreateKos");
        }

        kos.setPemilik(user);
        return service.create(kos)
                .thenApply(result -> {
                    ra.addFlashAttribute("success", "Kos berhasil dibuat");
                    return "redirect:daftarkos";
                })
                .exceptionally(throwable -> {
                    ra.addFlashAttribute("error", "Gagal membuat kos: " + throwable.getMessage());
                    return "redirect:/pemilik/create";
                });
    }

    @GetMapping("/edit/{id}")
    public CompletableFuture<String> editKosPage(@PathVariable UUID id, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        return service.findById(id)
                .thenApply(kos -> {
                    model.addAttribute("kos", kos);
                    return "pengelolaan/EditKos";
                })
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof PengelolaanRepository.KosNotFoundException) {
                        System.out.println("kos not found");
                        return "pengelolaan/error/KosNotFound";
                    }
                    ra.addFlashAttribute("error", "Gagal mengambil data kos: " + throwable.getMessage());
                    return "redirect:/pemilik/daftarkos";
                });
    }

    @PostMapping("/edit/{id}")
    public CompletableFuture<String> editKosPost(@PathVariable UUID id, @Valid @ModelAttribute Kos kos, BindingResult bindingResult, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("kos", kos);
            return CompletableFuture.completedFuture("pengelolaan/EditKos");
        }

        kos.setId(id);
        kos.setPemilik(user);
        return service.update(kos)
                .thenApply(result -> {
                    ra.addFlashAttribute("success", "Kos berhasil diperbarui");
                    return "redirect:/pemilik/daftarkos";
                })
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof PengelolaanRepository.KosNotFoundException) {
                        return "pengelolaan/error/KosNotFound";
                    }
                    ra.addFlashAttribute("error", "Gagal memperbarui kos: " + throwable.getMessage());
                    return "redirect:/pemilik/edit/" + id;
                });
    }

    @PostMapping("/delete/{id}")
    public CompletableFuture<String> deleteKos(@PathVariable UUID id, Model model, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        return service.findById(id)
                .thenCompose(kos -> service.delete(kos)
                        .thenApply(v -> {
                            ra.addFlashAttribute("success", "Kos berhasil dihapus");
                            return "redirect:/pemilik/daftarkos";
                        }))
                .exceptionally(throwable -> {
                    if (throwable.getCause() instanceof PengelolaanRepository.KosNotFoundException) {
                        model.addAttribute("errorMessage", "Kos dengan ID " + id + " tidak ditemukan.");
                        return "pengelolaan/error/KosNotFound";
                    }
                    ra.addFlashAttribute("error", "Gagal menghapus kos: " + throwable.getMessage());
                    return "redirect:/pemilik/daftarkos";
                });
    }

    @GetMapping("/daftarsewa")
    public CompletableFuture<String> daftarSewa(HttpSession session, RedirectAttributes ra, Model model) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            ra.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        model.addAttribute("user", user);
        return service.findAllSewa(user.getId())
                .thenApply(allSewa -> {
                    List<Penyewaan> filteredSewa = allSewa.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    model.addAttribute("allSewa", filteredSewa);
                    return "pengelolaan/ListSewa";
                })
                .exceptionally(throwable -> {
                    ra.addFlashAttribute("error", "Gagal memuat daftar sewa: " + throwable.getMessage());
                    return "redirect:/pemilik/home";
                });
    }

    @PostMapping("/ajuan-sewa/{id}")
    public CompletableFuture<String> acceptPenyewaan(@PathVariable String id, HttpSession session, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "ID penyewaan tidak valid");
            return CompletableFuture.completedFuture("redirect:/pemilik/daftarsewa");
        }

        return service.terimaSewa(id, user.getId())
                .thenApply(v -> {
                    ra.addFlashAttribute("success", "Penyewaan berhasil diterima");
                    return "redirect:/pemilik/daftarsewa";
                })
                .exceptionally(throwable -> {
                    ra.addFlashAttribute("error", "Gagal menerima penyewaan: " + throwable.getMessage());
                    return "redirect:/pemilik/daftarsewa";
                });
    }

    @PostMapping("/tolak-sewa/{id}")
    public CompletableFuture<String> rejectPenyewaan(@PathVariable String id, HttpSession session, RedirectAttributes ra, Model model) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return CompletableFuture.completedFuture("redirect:/api/auth/login");
        }

        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "ID penyewaan tidak valid");
            return CompletableFuture.completedFuture("redirect:/pemilik/daftarsewa");
        }

        return service.tolakSewa(id, user.getId())
                .thenApply(v -> {
                    ra.addFlashAttribute("success", "Penyewaan berhasil ditolak");
                    return "redirect:/pemilik/daftarsewa";
                })
                .exceptionally(throwable -> {
                    ra.addFlashAttribute("error", "Gagal menolak penyewaan: " + throwable.getMessage());
                    return "redirect:/pemilik/daftarsewa";
                });
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
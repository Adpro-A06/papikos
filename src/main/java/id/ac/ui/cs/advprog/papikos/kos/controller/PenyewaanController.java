package id.ac.ui.cs.advprog.papikos.kos.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.kos.model.penyewaan.Penyewaan;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.service.PenyewaanService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/penyewaan")
public class PenyewaanController {

    private final PenyewaanService penyewaanService;
    private final KosService kosService;
    private final AuthService authService;

    @Autowired
    public PenyewaanController(PenyewaanService penyewaanService,
            KosService kosService,
            AuthService authService) {
        this.penyewaanService = penyewaanService;
        this.kosService = kosService;
        this.authService = authService;
    }

    @GetMapping
    public String listPenyewaan(HttpSession session, Model model, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        List<Penyewaan> penyewaanList = penyewaanService.findByPenyewa(user);
        model.addAttribute("penyewaanList", penyewaanList);
        model.addAttribute("user", user);
        return "penyewaan/ListSewa";
    }

    @GetMapping("/new/{kosId}")
    public String newPenyewaanForm(@PathVariable String kosId,
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
            Kos kos = kosService.findById(kosId)
                    .orElseThrow(() -> new EntityNotFoundException("Kos tidak ditemukan"));

            Penyewaan penyewaan = new Penyewaan();
            penyewaan.setTanggalCheckIn(LocalDate.now().plusDays(1));
            penyewaan.setDurasiSewa(1);

            model.addAttribute("kos", kos);
            model.addAttribute("penyewaan", penyewaan);
            model.addAttribute("user", user);
            return "penyewaan/FormSewa";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewa/home";
        }
    }

    @PostMapping("/create/{kosId}")
    public String createPenyewaan(@PathVariable String kosId,
            @Valid @ModelAttribute Penyewaan penyewaan,
            BindingResult bindingResult,
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

        if (bindingResult.hasErrors()) {
            try {
                Kos kos = kosService.findById(kosId)
                        .orElseThrow(() -> new EntityNotFoundException("Kos tidak ditemukan"));
                model.addAttribute("kos", kos);
                model.addAttribute("user", user);
                return "penyewaan/FormSewa";
            } catch (EntityNotFoundException e) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:/penyewa/home";
            }
        }

        try {
            penyewaanService.createPenyewaan(penyewaan, kosId, user);
            ra.addFlashAttribute("success", "Pengajuan penyewaan berhasil dibuat");
            return "redirect:/penyewaan";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewaan/new/" + kosId;
        }
    }

    @GetMapping("/{id}/edit")
    public String editPenyewaanForm(@PathVariable String id,
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
            Penyewaan penyewaan = penyewaanService.findByIdAndPenyewa(id, user)
                    .orElseThrow(() -> new EntityNotFoundException("Penyewaan tidak ditemukan"));

            if (!penyewaanService.isPenyewaanEditable(penyewaan)) {
                ra.addFlashAttribute("error", "Penyewaan tidak dapat diedit");
                return "redirect:/penyewaan";
            }

            model.addAttribute("penyewaan", penyewaan);
            model.addAttribute("kos", penyewaan.getKos());
            model.addAttribute("user", user);
            return "penyewaan/EditSewa";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewaan";
        }
    }

    @PostMapping("/{id}/update")
    public String updatePenyewaan(@PathVariable String id,
            @Valid @ModelAttribute Penyewaan updatedPenyewaan,
            BindingResult bindingResult,
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

        if (bindingResult.hasErrors()) {
            model.addAttribute("kos", kosService.findById(updatedPenyewaan.getKos().getId()).orElse(null));
            model.addAttribute("user", user);
            return "penyewaan/EditSewa";
        }

        try {
            penyewaanService.updatePenyewaan(updatedPenyewaan, id, user);
            ra.addFlashAttribute("success", "Penyewaan berhasil diperbarui");
            return "redirect:/penyewaan";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewaan/" + id + "/edit";
        }
    }

    @GetMapping("/{id}")
    public String viewPenyewaan(@PathVariable String id,
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
            Penyewaan penyewaan = penyewaanService.findByIdAndPenyewa(id, user)
                    .orElseThrow(() -> new EntityNotFoundException("Penyewaan tidak ditemukan"));

            model.addAttribute("penyewaan", penyewaan);
            model.addAttribute("isEditable", penyewaanService.isPenyewaanEditable(penyewaan));
            model.addAttribute("isCancellable", penyewaanService.isPenyewaanCancellable(penyewaan));
            model.addAttribute("user", user);
            return "penyewaan/DetailSewa";
        } catch (EntityNotFoundException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewaan";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelPenyewaan(@PathVariable String id,
            HttpSession session,
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
            penyewaanService.cancelPenyewaan(id, user);
            ra.addFlashAttribute("success", "Penyewaan berhasil dibatalkan");
            return "redirect:/penyewaan";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/penyewaan/" + id;
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
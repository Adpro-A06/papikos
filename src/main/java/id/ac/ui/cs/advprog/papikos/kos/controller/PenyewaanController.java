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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/penyewaan")
public class PenyewaanController {

    private final PenyewaanService penyewaanService;
    private final KosService kosService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(PenyewaanController.class);

    @Autowired
    public PenyewaanController(PenyewaanService penyewaanService,
            KosService kosService,
            AuthService authService) {
        this.penyewaanService = penyewaanService;
        this.kosService = kosService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String listPenyewaan(HttpSession session, Model model, RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        try {
            List<Penyewaan> penyewaanList = penyewaanService.findByPenyewa(user).get();
            model.addAttribute("penyewaanList", penyewaanList);
            model.addAttribute("user", user);
            logger.info("User [{}] accessed [GET] /penyewaan/", user.getEmail());
            return "penyewaan/ListSewa";
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            ra.addFlashAttribute("error", "Gagal memuat data penyewaan: " + errorMessage);
            logger.error("Failed to load penyewaan for user [{}]: {}", user.getEmail(), errorMessage);
            return "redirect:/penyewa/home";
        }
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
            UUID kosUUID;
            try {
                kosUUID = UUID.fromString(kosId);
            } catch (IllegalArgumentException e) {
                throw new EntityNotFoundException("Format ID kos tidak valid");
            }

            Kos kos = kosService.findById(kosUUID)
                    .orElseThrow(() -> new EntityNotFoundException("Kos tidak ditemukan"));

            logger.info("User [{}] accessed [GET] /penyewaan/new/{}", user.getEmail(), kosId);

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
    public String createPenyewaan(
            @PathVariable String kosId,
            @RequestParam String namaLengkap,
            @RequestParam String nomorTelepon,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggalCheckIn,
            @RequestParam Integer durasiSewa,
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
            Penyewaan penyewaan = new Penyewaan();
            penyewaan.setNamaLengkap(namaLengkap);
            penyewaan.setNomorTelepon(nomorTelepon);
            penyewaan.setTanggalCheckIn(tanggalCheckIn);
            penyewaan.setDurasiSewa(durasiSewa);

            penyewaanService.createPenyewaan(penyewaan, kosId, user).get();
            logger.info("Penyewaan created successfully for user [{}]", user.getEmail());
            ra.addFlashAttribute("success", "Pengajuan penyewaan berhasil dibuat");
            return "redirect:/penyewaan/";
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            ra.addFlashAttribute("error", errorMessage);
            logger.error("Failed to create penyewaan for user [{}]: {}", user.getEmail(), errorMessage);
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
            Optional<Penyewaan> penyewaanOpt = penyewaanService.findByIdAndPenyewa(id, user).get();
            if (penyewaanOpt.isEmpty()) {
                throw new EntityNotFoundException("Penyewaan tidak ditemukan");
            }

            Penyewaan penyewaan = penyewaanOpt.get();
            if (!penyewaanService.isPenyewaanEditable(penyewaan)) {
                ra.addFlashAttribute("error", "Penyewaan tidak dapat diedit");
                return "redirect:/penyewaan/";
            }

            logger.info("User [{}] accessed [GET] /penyewaan/{}/edit", user.getEmail(), id);

            model.addAttribute("penyewaan", penyewaan);
            model.addAttribute("kos", penyewaan.getKos());
            model.addAttribute("user", user);
            return "penyewaan/EditSewa";
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            ra.addFlashAttribute("error", errorMessage);
            return "redirect:/penyewaan/";
        }
    }

    @PostMapping("/{id}/update")
    public String updatePenyewaan(
            @PathVariable String id,
            @RequestParam String namaLengkap,
            @RequestParam String nomorTelepon,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggalCheckIn,
            @RequestParam Integer durasiSewa,
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
            Penyewaan updatedPenyewaan = new Penyewaan();
            updatedPenyewaan.setNamaLengkap(namaLengkap);
            updatedPenyewaan.setNomorTelepon(nomorTelepon);
            updatedPenyewaan.setTanggalCheckIn(tanggalCheckIn);
            updatedPenyewaan.setDurasiSewa(durasiSewa);

            penyewaanService.updatePenyewaan(updatedPenyewaan, id, user).get();
            logger.info("Penyewaan [{}] updated successfully for user [{}]", id, user.getEmail());
            ra.addFlashAttribute("success", "Penyewaan berhasil diperbarui");
            return "redirect:/penyewaan/";
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            ra.addFlashAttribute("error", errorMessage);
            logger.error("Failed to update penyewaan [{}] for user [{}]: {}", id, user.getEmail(), errorMessage);
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
            Optional<Penyewaan> penyewaanOpt = penyewaanService.findByIdAndPenyewa(id, user).get();
            if (penyewaanOpt.isEmpty()) {
                throw new EntityNotFoundException("Penyewaan tidak ditemukan");
            }

            logger.info("User [{}] accessed [GET] /penyewaan/{}", user.getEmail(), id);

            Penyewaan penyewaan = penyewaanOpt.get();
            model.addAttribute("penyewaan", penyewaan);
            model.addAttribute("isEditable", penyewaanService.isPenyewaanEditable(penyewaan));
            model.addAttribute("isCancellable", penyewaanService.isPenyewaanCancellable(penyewaan));
            model.addAttribute("user", user);
            return "penyewaan/DetailSewa";
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            ra.addFlashAttribute("error", errorMessage);
            return "redirect:/penyewaan/";
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
            logger.info("Penyewaan [{}] cancelled successfully for user [{}]", id, user.getEmail());
            penyewaanService.cancelPenyewaan(id, user).get();
            ra.addFlashAttribute("success", "Penyewaan berhasil dibatalkan");
            return "redirect:/penyewaan/";
        } catch (InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            ra.addFlashAttribute("error", errorMessage);
            logger.error("Failed to cancel penyewaan [{}] for user [{}]: {}", id, user.getEmail(), errorMessage);
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
package id.ac.ui.cs.advprog.papikos.controller;

import id.ac.ui.cs.advprog.papikos.model.Kos;
import id.ac.ui.cs.advprog.papikos.repository.PengelolaanRepository;
import id.ac.ui.cs.advprog.papikos.service.PengelolaanService;
import jakarta.servlet.http.HttpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/pemilik")
public class PengelolaanController {
    private final PengelolaanService service;

    @Autowired
    public PengelolaanController(PengelolaanService service) {
        this.service = service;
    }

    @GetMapping("/mainpage")
    public String mainPage(Model model) {
        return "home/PemilikKosHome";
    }

    @GetMapping("/create")
    public String createKosPage(Model model) {
        Kos kos = new Kos();
        model.addAttribute("kos", kos);
        return "pengelolaan/CreateKos";
    }

    @GetMapping("/daftarkos")
    public String kosListPage(Model model) {
        List<Kos> allKos = service.findAll();
        model.addAttribute("kos", allKos);
        return "pengelolaan/ListKos";
    }

    @PostMapping("/create")
    public String createKosPost(@ModelAttribute Kos kos, Model model) {
        service.create(kos);
        return "redirect:daftarkos";
    }

    @GetMapping("/edit/{id}")
    public String editKosPage(@PathVariable String id, Model model) {
        try {
            Kos kos = service.findById(id);
            model.addAttribute("kos", kos);
        }
        catch (PengelolaanRepository.KosNotFoundException e) {
            System.out.println("kos not found");
            return "error/KosNotFound";
        }
        return "pengelolaan/EditKos";
    }

    @PostMapping("/edit/{id}")
    public String editKosPost(@PathVariable String id, @ModelAttribute Kos kos, Model model) {
        try {
            kos.setId(id);
            service.update(kos);
        }
        catch (PengelolaanRepository.KosNotFoundException e) {
            System.out.println("kos not found");
            return "pengelolaan/error/KosNotFound";
        }
        return "redirect:daftarkos";
    }

    @GetMapping("/delete/{id}")
    public String deleteKos(@PathVariable String id, Model model) {
        try {
            Kos kos = service.findById(id);
            service.delete(kos);
            return "redirect:daftarkos";
        } catch (PengelolaanRepository.KosNotFoundException e) {
            System.out.println("kos not found");
            return "error/KosNotFound";
        }
    }
}
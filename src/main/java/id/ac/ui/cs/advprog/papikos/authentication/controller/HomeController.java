package id.ac.ui.cs.advprog.papikos.authentication.controller;

import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/api/auth/login-page";
    }
    
    @GetMapping("/admin/home")
    public String adminHome() {
        return "home/AdminHome";
    }

    @GetMapping("/pemilik/home")
    public String pemilikKosHome() {
        return "home/PemilikKosHome";
    }

    @GetMapping("/penyewa/home")
    public String penyewaHome() {
        return "home/PenyewaHome";
    }

    @GetMapping("/redirectAfterLogin")
    public String redirectAfterLogin(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_" + Role.ADMIN.name())) {
                    return "home/AdminHome";
                } else if (role.equals("ROLE_" + Role.PEMILIK_KOS.name())) {
                    return "home/PemilikKosHome";
                } else if (role.equals("ROLE_" + Role.PENYEWA.name())) {
                    return "home/PenyewaHome";
                }
            }
        }
        return "redirect:/api/auth/login";
    }
}
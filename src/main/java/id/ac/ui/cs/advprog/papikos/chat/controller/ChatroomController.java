package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomService;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Controller
public class ChatroomController {

    private final ChatroomService chatroomService;
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(ChatroomController.class);

    @Autowired
    public ChatroomController(ChatroomService chatroomService, AuthService authService) {
        this.chatroomService = chatroomService;
        this.authService = authService;
    }

    @GetMapping("/chatrooms/renter/{renterId}")
    public String getChatroomsByRenterId(@PathVariable UUID renterId,
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

        List<Chatroom> chatrooms = chatroomService.getChatroomsByRenterId(renterId);
        model.addAttribute("chatrooms", chatrooms);
        model.addAttribute("user", user);
        logger.info("Loaded {} chatrooms for renter [{}]", chatrooms.size(), user.getEmail());
        return "chat/ChatroomList";  // Template untuk menampilkan daftar chatroom
    }

    @GetMapping("/chatrooms/owner/{ownerId}")
    public String getChatroomsByOwnerId(@PathVariable UUID ownerId,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        List<Chatroom> chatrooms = chatroomService.getChatroomsByOwnerId(ownerId);
        model.addAttribute("chatrooms", chatrooms);
        model.addAttribute("user", user);
        logger.info("Loaded {} chatrooms for owner [{}]", chatrooms.size(), user.getEmail());
        return "chat/ChatroomList";
    }

    @GetMapping("/chatrooms/{id}")
    public String viewChatroomDetail(@PathVariable UUID id,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes ra) {
        User user = getCurrentUser(session, ra);
        if (user == null) {
            return "redirect:/api/auth/login";
        }

        if (user.getRole() != Role.PENYEWA && user.getRole() != Role.PEMILIK_KOS) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        try {
            Chatroom chatroom = chatroomService.getChatroomById(id);
            model.addAttribute("chatroom", chatroom);
            model.addAttribute("user", user);
            logger.info("Loaded chatroom [{}] details successfully for user [{}]", id, user.getEmail());
            return "chat/Chatroom";  // Template untuk menampilkan detail chatroom
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Chatroom tidak ditemukan");
            logger.error("Chatroom [{}] not found for user [{}]", id, user.getEmail());
            return "redirect:/chatrooms/renter/" + user.getId();  // Redirect ke daftar chatroom renter
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

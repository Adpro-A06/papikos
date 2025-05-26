package id.ac.ui.cs.advprog.papikos.chat.controller;

import id.ac.ui.cs.advprog.papikos.chat.model.Chatroom;
import id.ac.ui.cs.advprog.papikos.chat.service.ChatroomService;
import id.ac.ui.cs.advprog.papikos.authentication.model.Role;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.service.KosService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
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
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
public class ChatroomController {

    private final ChatroomService chatroomService;
    private final AuthService authService;
    private final KosService kosService;
    private static final Logger logger = LoggerFactory.getLogger(ChatroomController.class);

    @Autowired
    public ChatroomController(ChatroomService chatroomService, AuthService authService, KosService kosService) {
        this.chatroomService = chatroomService;
        this.authService = authService;
        this.kosService = kosService;
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

        if (user.getRole() != Role.PENYEWA || !user.getId().equals(renterId)) {
            ra.addFlashAttribute("error", "Anda tidak memiliki akses ke halaman ini");
            return "redirect:/";
        }

        List<Chatroom> chatrooms = chatroomService.getChatroomsByRenterId(renterId);
        Map<String, Object> chatroomData = prepareChatroomData(chatrooms);

        model.addAttribute("chatrooms", chatrooms);
        model.addAttribute("chatroomData", chatroomData);
        model.addAttribute("user", user);
        logger.info("Loaded {} chatrooms for renter [{}]", chatrooms.size(), user.getEmail());
        return "chat/ChatroomList";
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
        Map<String, Object> chatroomData = prepareChatroomData(chatrooms);

        model.addAttribute("chatrooms", chatrooms);
        model.addAttribute("chatroomData", chatroomData);
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

            String renterName = getUserEmailById(chatroom.getRenterId());
            String ownerName = getUserEmailById(chatroom.getOwnerId());
            String propertyName = getPropertyNameById(chatroom.getPropertyId());

            model.addAttribute("chatroom", chatroom);
            model.addAttribute("renterName", renterName);
            model.addAttribute("ownerName", ownerName);
            model.addAttribute("propertyName", propertyName);
            model.addAttribute("user", user);

            logger.info("Loaded chatroom [{}] details successfully for user [{}]", id, user.getEmail());
            return "chat/Chatroom";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Chatroom tidak ditemukan");
            logger.error("Chatroom [{}] not found for user [{}]", id, user.getEmail());
            return "redirect:/chatrooms/renter/" + user.getId();
        }
    }

    private Map<String, Object> prepareChatroomData(List<Chatroom> chatrooms) {
        Map<String, Object> data = new HashMap<>();
        Map<String, String> userNames = new HashMap<>();
        Map<String, String> propertyNames = new HashMap<>();

        for (Chatroom chatroom : chatrooms) {
            if (!userNames.containsKey(chatroom.getRenterId().toString())) {
                userNames.put(chatroom.getRenterId().toString(), getUserEmailById(chatroom.getRenterId()));
            }
            if (!userNames.containsKey(chatroom.getOwnerId().toString())) {
                userNames.put(chatroom.getOwnerId().toString(), getUserEmailById(chatroom.getOwnerId()));
            }
            if (!propertyNames.containsKey(chatroom.getPropertyId().toString())) {
                propertyNames.put(chatroom.getPropertyId().toString(), getPropertyNameById(chatroom.getPropertyId()));
            }
        }

        data.put("userNames", userNames);
        data.put("propertyNames", propertyNames);
        return data;
    }

    private String getUserEmailById(UUID userId) {
        try {
            User user = authService.findById(userId);
            return user != null ? user.getEmail().split("@")[0] : "Unknown User";
        } catch (Exception e) {
            logger.warn("Failed to get user name for ID: {}", userId);
            return "Unknown User";
        }
    }

    private String getPropertyNameById(UUID propertyId) {
        try {
            Optional<Kos> kosOptional = kosService.findById(propertyId);
            if (kosOptional.isPresent()) {
                Kos kos = kosOptional.get();
                return kos.getNama();
            }
            return "Unknown Property";
        } catch (Exception e) {
            logger.warn("Failed to get property name for ID: {}", propertyId, e);
            return "Property-" + propertyId.toString().substring(0, 8);
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
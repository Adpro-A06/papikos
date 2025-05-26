package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.dto.AuthDto;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.model.Wishlist;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import id.ac.ui.cs.advprog.papikos.wishlist.observer.WishlistSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlist")
public class WishlistRestController {

   private final WishlistService wishlistService;
   private final AuthService authService;
   private final WishlistSubject wishlistSubject;

   @PostMapping("/add")
   public ResponseEntity<?> addToWishlist(
           @RequestBody Map<String, Object> request,
           @RequestHeader("Authorization") String authHeader) {
       
       try {
           User user = getCurrentUser(authHeader);
           if (user == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
           }

           if (request == null || !request.containsKey("kosId")) {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "kosId is required"));
           }

           Long kosId = parseKosId(request.get("kosId"));
           if (kosId == null || kosId <= 0) {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "kosId must be a positive number"));
           }

           if (wishlistService.isInWishlist(user.getId(), kosId)) {
               return ResponseEntity.ok(new AuthDto.ApiResponse(false, "Kos sudah ada di wishlist"));
           }

           String userId = user.getId().toString();
           Wishlist wishlist = createWishlistWithKos(userId, kosId);
           Wishlist savedWishlist = wishlistService.addToWishlist(wishlist);

           if (savedWishlist != null) {
               wishlistSubject.notifyObservers(wishlist, "added");

               Map<String, Object> data = new HashMap<>();
               data.put("userId", user.getId());
               data.put("kosId", kosId);
               data.put("added", true);
               data.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

               return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Berhasil ditambahkan ke wishlist", data));
           } else {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "Gagal menambahkan ke wishlist"));
           }

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new AuthDto.ApiResponse(false, "Gagal menambahkan ke wishlist: " + e.getMessage()));
       }
   }

   @PostMapping("/remove")
   public ResponseEntity<?> removeFromWishlist(
           @RequestBody Map<String, Object> request,
           @RequestHeader("Authorization") String authHeader) {
       
       try {
           User user = getCurrentUser(authHeader);
           if (user == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
           }

           if (request == null || !request.containsKey("kosId")) {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "kosId is required"));
           }

           Long kosId = parseKosId(request.get("kosId"));
           if (kosId == null || kosId <= 0) {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "kosId must be a positive number"));
           }

           boolean removed = wishlistService.removeFromWishlist(user.getId(), kosId);

           if (removed) {
               String userId = user.getId().toString();
               Wishlist removedWishlist = createWishlistWithKos(userId, kosId);
               wishlistSubject.notifyObservers(removedWishlist, "removed");

               Map<String, Object> data = new HashMap<>();
               data.put("userId", user.getId());
               data.put("kosId", kosId);
               data.put("removed", true);
               data.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

               return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Berhasil dihapus dari wishlist", data));
           } else {
               return ResponseEntity.ok(new AuthDto.ApiResponse(false, "Item tidak ditemukan di wishlist"));
           }

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new AuthDto.ApiResponse(false, "Gagal menghapus dari wishlist: " + e.getMessage()));
       }
   }

   @PostMapping("/toggle")
   public ResponseEntity<?> toggleWishlist(
           @RequestBody Map<String, Object> request,
           @RequestHeader("Authorization") String authHeader) {
       
       try {
           User user = getCurrentUser(authHeader);
           if (user == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
           }

           if (request == null || !request.containsKey("kosId")) {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "kosId is required"));
           }

           Long kosId = parseKosId(request.get("kosId"));
           if (kosId == null || kosId <= 0) {
               return ResponseEntity.badRequest()
                       .body(new AuthDto.ApiResponse(false, "kosId must be a positive number"));
           }

           String userId = user.getId().toString();
           boolean inWishlist = wishlistService.isInWishlist(user.getId(), kosId);

           Map<String, Object> data = new HashMap<>();
           data.put("userId", user.getId());
           data.put("kosId", kosId);

           if (inWishlist) {
               wishlistService.removeFromWishlist(user.getId(), kosId);
               Wishlist removedWishlist = createWishlistWithKos(userId, kosId);
               wishlistSubject.notifyObservers(removedWishlist, "removed");
               data.put("added", false);
               data.put("action", "removed");
           } else {
               Wishlist wishlist = createWishlistWithKos(userId, kosId);
               wishlistService.addToWishlist(wishlist);
               wishlistSubject.notifyObservers(wishlist, "added");
               data.put("added", true);
               data.put("action", "added");
           }

           data.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

           return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist berhasil diupdate", data));

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new AuthDto.ApiResponse(false, "Gagal mengupdate wishlist: " + e.getMessage()));
       }
   }

   @PostMapping("/clear")
   public ResponseEntity<?> clearWishlist(@RequestHeader("Authorization") String authHeader) {
       try {
           User user = getCurrentUser(authHeader);
           if (user == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
           }

           wishlistService.clearUserWishlist(user.getId());

           String userId = user.getId().toString();
           Wishlist clearedWishlist = new Wishlist("Cleared Wishlist", userId);
           wishlistSubject.notifyObservers(clearedWishlist, "cleared");

           Map<String, Object> data = new HashMap<>();
           data.put("userId", user.getId());
           data.put("wishlistCount", 0);

           return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist berhasil dikosongkan", data));

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new AuthDto.ApiResponse(false, "Gagal mengosongkan wishlist: " + e.getMessage()));
       }
   }

   @GetMapping("/user")
   public ResponseEntity<?> getUserWishlist(@RequestHeader("Authorization") String authHeader) {
       try {
           User user = getCurrentUser(authHeader);
           if (user == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
           }

           List<Wishlist> wishlists = wishlistService.getUserWishlist(user.getId());

           Map<String, Object> data = new HashMap<>();
           data.put("userId", user.getId());
           data.put("wishlistItems", wishlists);
           data.put("wishlistCount", wishlists.size());

           return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist berhasil diambil", data));

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new AuthDto.ApiResponse(false, "Gagal mengambil wishlist: " + e.getMessage()));
       }
   }

   @GetMapping("/user-ids")
   public ResponseEntity<?> getUserWishlistIds(@RequestHeader("Authorization") String authHeader) {
       try {
           User user = getCurrentUser(authHeader);
           if (user == null) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
           }

           List<Long> kosIds = wishlistService.getUserWishlistKosIdsAsLong(user.getId());

           Map<String, Object> data = new HashMap<>();
           data.put("userId", user.getId());
           data.put("kosIds", kosIds);

           return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist IDs berhasil diambil", data));

       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(new AuthDto.ApiResponse(false, "Gagal mengambil wishlist IDs: " + e.getMessage()));
       }
   }

   private Wishlist createWishlistWithKos(String userId, Long kosId) {
       if (userId == null || userId.trim().isEmpty()) {
           throw new IllegalArgumentException("UserId cannot be null or empty");
       }
       if (kosId == null || kosId <= 0) {
           throw new IllegalArgumentException("KosId must be a positive number");
       }

       Kos kos = new Kos();
       UUID kosUuid = new UUID(kosId, kosId);
       kos.setId(kosUuid);

       Wishlist wishlist = new Wishlist("User Wishlist", userId);
 
       List<Kos> kosList = new ArrayList<>();
       kosList.add(kos);
       wishlist.setKosList(kosList);
       
       return wishlist;
   }

   private User getCurrentUser(String authHeader) {
       try {
           String token = extractTokenFromHeader(authHeader);
           if (token == null || token.trim().isEmpty()) {
               return null;
           }

           String idStr = authService.decodeToken(token);
           if (idStr == null || idStr.trim().isEmpty()) {
               return null;
           }
           
           return authService.findById(UUID.fromString(idStr));
       } catch (Exception e) {
           return null;
       }
   }

   private String extractTokenFromHeader(String authHeader) {
       if (authHeader == null || !authHeader.startsWith("Bearer ")) {
           return null;
       }
       
       String token = authHeader.substring(7);
       return token.trim().isEmpty() ? null : token;
   }

   private Long parseKosId(Object kosIdObj) {
       if (kosIdObj == null) {
           return null;
       }
       
       try {
           if (kosIdObj instanceof Integer) {
               return ((Integer) kosIdObj).longValue();
           } else if (kosIdObj instanceof Long) {
               return (Long) kosIdObj;
           } else if (kosIdObj instanceof String) {
               String str = ((String) kosIdObj).trim();
               if (str.isEmpty()) {
                   return null;
               }
               return Long.parseLong(str);
           }
       } catch (NumberFormatException e) {
           return null;
       }
       return null;
   }
}
package id.ac.ui.cs.advprog.papikos.wishlist.controller;

import id.ac.ui.cs.advprog.papikos.authentication.dto.AuthDto;
import id.ac.ui.cs.advprog.papikos.authentication.model.User;
import id.ac.ui.cs.advprog.papikos.authentication.service.AuthService;
import id.ac.ui.cs.advprog.papikos.kos.model.Kos;
import id.ac.ui.cs.advprog.papikos.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishlist")
public class WishlistRestController {

    private static final Logger log = LoggerFactory.getLogger(WishlistRestController.class);

    private final WishlistService wishlistService;
    private final AuthService authService;

    @PostMapping("/add")
    public ResponseEntity<?> addToWishlist(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                log.warn("Unauthorized add to wishlist attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (request == null || !request.containsKey("kosId")) {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "kosId is required"));
            }

            UUID kosId = parseKosIdAsUUID(request.get("kosId"));
            if (kosId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "kosId must be a valid UUID"));
            }

            log.info("API add to wishlist - User: {}, KosId: {}", user.getId(), kosId);

            if (wishlistService.isInWishlist(user.getId(), kosId)) {
                return ResponseEntity.ok(new AuthDto.ApiResponse(false, "Kos sudah ada di wishlist"));
            }

            wishlistService.addToWishlist(user.getId(), kosId);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("kosId", kosId);
            data.put("added", true);
            data.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

            log.info("Successfully added kos {} to wishlist via API for user {}", kosId, user.getId());
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Berhasil ditambahkan ke wishlist", data));

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in API addToWishlist: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error in API addToWishlist: {}", e.getMessage(), e);
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
                log.warn("Unauthorized remove from wishlist attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (request == null || !request.containsKey("kosId")) {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "kosId is required"));
            }

            UUID kosId = parseKosIdAsUUID(request.get("kosId"));
            if (kosId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "kosId must be a valid UUID"));
            }

            log.info("API remove from wishlist - User: {}, KosId: {}", user.getId(), kosId);

            if (!wishlistService.isInWishlist(user.getId(), kosId)) {
                return ResponseEntity.ok(new AuthDto.ApiResponse(false, "Item tidak ditemukan di wishlist"));
            }

            wishlistService.removeFromWishlist(user.getId(), kosId);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("kosId", kosId);
            data.put("removed", true);
            data.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

            log.info("Successfully removed kos {} from wishlist via API for user {}", kosId, user.getId());
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Berhasil dihapus dari wishlist", data));

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in API removeFromWishlist: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error in API removeFromWishlist: {}", e.getMessage(), e);
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
                log.warn("Unauthorized toggle wishlist attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            if (request == null || !request.containsKey("kosId")) {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "kosId is required"));
            }

            UUID kosId = parseKosIdAsUUID(request.get("kosId"));
            if (kosId == null) {
                return ResponseEntity.badRequest()
                        .body(new AuthDto.ApiResponse(false, "kosId must be a valid UUID"));
            }

            log.info("API toggle wishlist - User: {}, KosId: {}", user.getId(), kosId);

            boolean wasInWishlist = wishlistService.isInWishlist(user.getId(), kosId);
            wishlistService.toggleWishlist(user.getId(), kosId);
            boolean isNowInWishlist = wishlistService.isInWishlist(user.getId(), kosId);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("kosId", kosId);
            data.put("added", isNowInWishlist);
            data.put("action", isNowInWishlist ? "added" : "removed");
            data.put("wishlistCount", wishlistService.getWishlistCount(user.getId()));

            String action = isNowInWishlist ? "ditambahkan ke" : "dihapus dari";
            log.info("Successfully toggled kos {} {} wishlist via API for user {}", kosId, action, user.getId());
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist berhasil diupdate", data));

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in API toggleWishlist: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error in API toggleWishlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthDto.ApiResponse(false, "Gagal mengupdate wishlist: " + e.getMessage()));
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clearWishlist(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                log.warn("Unauthorized clear wishlist attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            log.info("API clear wishlist for user: {}", user.getId());
            
            int countBefore = wishlistService.getWishlistCount(user.getId());
            wishlistService.clearUserWishlist(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("wishlistCount", 0);
            data.put("clearedCount", countBefore);

            log.info("Successfully cleared {} items from wishlist via API for user {}", countBefore, user.getId());
            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist berhasil dikosongkan", data));

        } catch (IllegalArgumentException e) {
            log.warn("Validation error in API clearWishlist: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error in API clearWishlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthDto.ApiResponse(false, "Gagal mengosongkan wishlist: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserWishlist(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                log.warn("Unauthorized get user wishlist attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            log.debug("API get user wishlist for user: {}", user.getId());
            
            List<Kos> wishlistItems = wishlistService.getUserWishlistItems(user.getId());
            int wishlistCount = wishlistService.getWishlistCount(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("wishlistItems", wishlistItems);
            data.put("wishlistCount", wishlistCount);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist berhasil diambil", data));

        } catch (Exception e) {
            log.error("Error in API getUserWishlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthDto.ApiResponse(false, "Gagal mengambil wishlist: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getWishlistCount(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                log.warn("Unauthorized get wishlist count attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            int count = wishlistService.getWishlistCount(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("wishlistCount", count);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Wishlist count berhasil diambil", data));

        } catch (Exception e) {
            log.error("Error in API getWishlistCount: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthDto.ApiResponse(false, "Gagal mengambil wishlist count: " + e.getMessage()));
        }
    }

    @GetMapping("/check/{kosId}")
    public ResponseEntity<?> checkWishlist(
            @PathVariable String kosId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            User user = getCurrentUser(authHeader);
            if (user == null) {
                log.warn("Unauthorized check wishlist attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthDto.ApiResponse(false, "Token tidak valid"));
            }

            UUID kosUuid = UUID.fromString(kosId);
            boolean inWishlist = wishlistService.isInWishlist(user.getId(), kosUuid);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("kosId", kosUuid);
            data.put("inWishlist", inWishlist);

            return ResponseEntity.ok(new AuthDto.ApiResponse(true, "Status wishlist berhasil dicek", data));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.ApiResponse(false, "Invalid kosId format"));
        } catch (Exception e) {
            log.error("Error in API checkWishlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthDto.ApiResponse(false, "Gagal mengecek status wishlist: " + e.getMessage()));
        }
    }

    // Helper methods
    private UUID parseKosIdAsUUID(Object kosIdObj) {
        if (kosIdObj == null) {
            return null;
        }
        
        try {
            if (kosIdObj instanceof String) {
                String str = ((String) kosIdObj).trim();
                if (str.isEmpty()) {
                    return null;
                }
                return UUID.fromString(str);
            } else if (kosIdObj instanceof UUID) {
                return (UUID) kosIdObj;
            } else {
                // Try to convert to string first
                return UUID.fromString(kosIdObj.toString());
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", kosIdObj);
            return null;
        }
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
            log.error("Error getting current user from auth header: {}", e.getMessage());
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
}
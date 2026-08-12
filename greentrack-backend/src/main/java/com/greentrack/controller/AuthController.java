package com.greentrack.controller;
import com.greentrack.dto.request.*;
import com.greentrack.dto.response.*;
import com.greentrack.entity.User;
import com.greentrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Account created", authService.register(req)));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(req)));
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String,String> body) {
        String token = body.get("refreshToken");
        if (token == null) return ResponseEntity.badRequest().body(ApiResponse.error("refreshToken required"));
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refreshToken(token)));
    }
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> fcmToken(@AuthenticationPrincipal User user, @RequestBody Map<String,String> body) {
        authService.updateFcmToken(user.getId(), body.get("fcmToken"));
        return ResponseEntity.ok(ApiResponse.ok("FCM token updated", null));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserSummary>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(AuthResponse.UserSummary.builder()
            .id(user.getId()).name(user.getName()).email(user.getEmail())
            .phone(user.getPhone()).role(user.getRole()).badgeId(user.getBadgeId()).build()));
    }
}
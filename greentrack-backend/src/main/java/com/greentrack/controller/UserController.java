package com.greentrack.controller;
import com.greentrack.dto.request.CreateStaffRequest;
import com.greentrack.dto.response.*;
import com.greentrack.entity.User;
import com.greentrack.exception.ResourceNotFoundException;
import com.greentrack.repository.UserRepository;
import com.greentrack.service.AuthService;
import com.greentrack.web.PageRequestUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse>> createStaff(@Valid @RequestBody CreateStaffRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Staff account created", authService.createStaff(req)));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuthResponse.UserSummary>>> list(
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
            userRepository.findAll(PageRequestUtil.of(page, size, Sort.by("createdAt").descending()))
                .map(u -> toSummary(u))));
    }
    @GetMapping("/collectors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthResponse.UserSummary>>> collectors() {
        return ResponseEntity.ok(ApiResponse.ok(
            userRepository.findByRoleAndIsActiveTrue(User.Role.COLLECTOR).stream()
                .map(u -> toSummary(u)).toList()));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse.UserSummary>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(toSummary(
            userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User",id)))));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setStatus(@PathVariable Long id, @RequestBody Map<String,Boolean> body) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User",id));
        Boolean active = body.get("active");
        if (active == null) return ResponseEntity.badRequest().body(ApiResponse.error("'active' required"));
        u.setActive(active); userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.ok("User " + (active?"activated":"deactivated"), null));
    }

    private AuthResponse.UserSummary toSummary(User u) {
        return AuthResponse.UserSummary.builder().id(u.getId()).name(u.getName())
            .email(u.getEmail()).phone(u.getPhone()).role(u.getRole()).badgeId(u.getBadgeId()).build();
    }
}
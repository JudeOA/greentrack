package com.greentrack.controller;
import com.greentrack.dto.response.*;
import com.greentrack.entity.User;
import com.greentrack.service.NotificationService;
import com.greentrack.web.PageRequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getForUser(user, PageRequestUtil.of(page, size))));
    }
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String,Long>>> count(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", notificationService.getUnreadCount(user))));
    }
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.markRead(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Marked read", null));
    }
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user);
        return ResponseEntity.ok(ApiResponse.ok("All marked read", null));
    }
}
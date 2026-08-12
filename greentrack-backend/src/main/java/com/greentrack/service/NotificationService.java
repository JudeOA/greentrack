package com.greentrack.service;
import com.greentrack.dto.response.NotificationResponse;
import com.greentrack.entity.*;
import com.greentrack.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    @Transactional
    public void notifyReportReceived(Report r) {
        saveAndPush(r.getCitizen(), r, Notification.Type.REPORT_RECEIVED,
            "Report Received ✅", "Your report \"" + r.getTitle() + "\" is under review.");
    }
    @Transactional
    public void notifyReportAssigned(Report r, User collector) {
        saveAndPush(r.getCitizen(), r, Notification.Type.REPORT_ASSIGNED,
            "Report Assigned 👷", "A collector has been assigned to your report.");
        saveAndPush(collector, r, Notification.Type.REPORT_ASSIGNED,
            "New Task 📍", "New cleanup task: " + r.getTitle() + " at " + r.getAddress());
    }
    @Transactional
    public void notifyReportResolved(Report r) {
        saveAndPush(r.getCitizen(), r, Notification.Type.REPORT_RESOLVED,
            "Issue Resolved ✅", "Your report \"" + r.getTitle() + "\" has been resolved. Thank you! 🌿");
    }
    @Transactional
    public void notifyReportRejected(Report r, String reason) {
        saveAndPush(r.getCitizen(), r, Notification.Type.REPORT_REJECTED,
            "Report Not Accepted ❌", "Your report could not be processed" + (reason != null ? ": " + reason : "."));
    }
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getForUser(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) { return notificationRepository.countByUserAndReadFalse(user); }
    @Transactional
    public void markRead(Long id, User user) {
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) { n.setRead(true); notificationRepository.save(n); }
        });
    }
    @Transactional
    public void markAllRead(User user) { notificationRepository.markAllReadByUser(user); }

    private void saveAndPush(User user, Report report, Notification.Type type, String title, String message) {
        notificationRepository.save(Notification.builder()
            .user(user).report(report).title(title).message(message).type(type).read(false).build());
        if (user.getFcmToken() != null) fcmService.sendPush(user.getFcmToken(), title, message);
    }
    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder().id(n.getId()).title(n.getTitle()).message(n.getMessage())
            .type(n.getType()).read(n.isRead())
            .reportId(n.getReport() != null ? n.getReport().getId() : null)
            .createdAt(n.getCreatedAt()).build();
    }
}
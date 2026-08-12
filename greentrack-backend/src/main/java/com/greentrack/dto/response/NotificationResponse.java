package com.greentrack.dto.response;
import com.greentrack.entity.Notification;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private Notification.Type type;
    private boolean read;
    private Long reportId;
    private LocalDateTime createdAt;
}
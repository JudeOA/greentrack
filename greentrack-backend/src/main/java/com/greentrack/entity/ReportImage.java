package com.greentrack.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "report_images")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "report_id", nullable = false) private Report report;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uploaded_by", nullable = false) private User uploadedBy;
    @Column(name = "image_url", nullable = false, length = 500) private String imageUrl;
    @Column(name = "is_before_cleanup", nullable = false) @Builder.Default private boolean beforeCleanup = true;
    @CreationTimestamp @Column(name = "uploaded_at", nullable = false, updatable = false) private LocalDateTime uploadedAt;
}
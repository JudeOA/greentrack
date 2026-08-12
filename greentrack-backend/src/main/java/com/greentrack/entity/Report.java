package com.greentrack.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "reports")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "citizen_id", nullable = false) private User citizen;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false, precision = 10, scale = 8) private BigDecimal latitude;
    @Column(nullable = false, precision = 11, scale = 8) private BigDecimal longitude;
    @Column(length = 300) private String address;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private Status status = Status.PENDING;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private Priority priority = Priority.MEDIUM;
    @Column(name = "is_deleted", nullable = false) @Builder.Default private boolean deleted = false;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "resolved_at") private LocalDateTime resolvedAt;
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default private List<ReportImage> images = new ArrayList<>();
    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY) private Assignment assignment;
    public enum Status   { PENDING, IN_PROGRESS, RESOLVED, REJECTED }
    public enum Priority { LOW, MEDIUM, HIGH }
}
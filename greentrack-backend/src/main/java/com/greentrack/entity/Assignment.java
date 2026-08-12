package com.greentrack.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "assignments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Assignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "report_id", nullable = false, unique = true) private Report report;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "collector_id", nullable = false) private User collector;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_by", nullable = false) private User assignedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private Status status = Status.ASSIGNED;
    @Column(columnDefinition = "TEXT") private String notes;
    @CreationTimestamp @Column(name = "assigned_at", nullable = false, updatable = false) private LocalDateTime assignedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    public enum Status { ASSIGNED, EN_ROUTE, COMPLETED }
}
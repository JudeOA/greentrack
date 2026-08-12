package com.greentrack.dto.response;
import com.greentrack.entity.Report;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ReportResponse {
    private Long id;
    private String title;
    private String description;
    private CategoryInfo category;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private Report.Status status;
    private Report.Priority priority;
    private CitizenInfo citizen;
    private List<String> imageUrls;
    private AssignmentInfo assignment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    @Data @Builder public static class CategoryInfo { private Integer id; private String name; private String icon; }
    @Data @Builder public static class CitizenInfo  { private Long id; private String name; }
    @Data @Builder public static class AssignmentInfo {
        private Long id; private String collectorName; private String status; private LocalDateTime assignedAt;
    }
}
package com.greentrack.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class DashboardStats {
    private long totalReports;
    private long pendingReports;
    private long inProgressReports;
    private long resolvedReports;
    private long rejectedReports;
    private double resolutionRate;
    private List<HeatmapPoint> heatmapPoints;
    private List<ReportResponse> recentReports;

    @Data @Builder
    public static class HeatmapPoint {
        private BigDecimal latitude; private BigDecimal longitude; private int weight; private String status;
    }
}
package com.greentrack.service;
import com.greentrack.dto.response.DashboardStats;
import com.greentrack.entity.Report;
import com.greentrack.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class AnalyticsService {
    private final ReportRepository reportRepository;
    private final ReportService reportService;

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long total      = reportRepository.countByDeletedFalse();
        long pending    = reportRepository.countByStatusAndDeletedFalse(Report.Status.PENDING);
        long inProgress = reportRepository.countByStatusAndDeletedFalse(Report.Status.IN_PROGRESS);
        long resolved   = reportRepository.countByStatusAndDeletedFalse(Report.Status.RESOLVED);
        long rejected   = reportRepository.countByStatusAndDeletedFalse(Report.Status.REJECTED);
        double rate     = total > 0 ? Math.round((resolved/(double)total)*1000.0)/10.0 : 0.0;

        var heatmap = reportRepository.findActiveForHeatmap().stream()
            .map(r -> DashboardStats.HeatmapPoint.builder()
                .latitude(r.getLatitude()).longitude(r.getLongitude())
                .weight(r.getPriority()==Report.Priority.HIGH?3:r.getPriority()==Report.Priority.MEDIUM?2:1)
                .status(r.getStatus().name()).build())
            .collect(Collectors.toList());

        var recent = reportRepository.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0,10))
                .map(reportService::toResponse).getContent();

        return DashboardStats.builder().totalReports(total).pendingReports(pending)
            .inProgressReports(inProgress).resolvedReports(resolved).rejectedReports(rejected)
            .resolutionRate(rate).heatmapPoints(heatmap).recentReports(recent).build();
    }
}
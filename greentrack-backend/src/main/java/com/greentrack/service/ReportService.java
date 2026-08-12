package com.greentrack.service;
import com.greentrack.dto.request.*;
import com.greentrack.dto.response.ReportResponse;
import com.greentrack.entity.*;
import com.greentrack.exception.*;
import com.greentrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final NotificationService notificationService;

    @Transactional
    public ReportResponse create(ReportRequest req, List<MultipartFile> images, User citizen) {
        Category cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", Long.valueOf(req.getCategoryId())));
        Report report = Report.builder().citizen(citizen).category(cat).title(req.getTitle())
                .description(req.getDescription()).latitude(req.getLatitude()).longitude(req.getLongitude())
                .address(req.getAddress()).priority(req.getPriority()).status(Report.Status.PENDING).build();
        report = reportRepository.save(report);
        if (images != null) {
            for (MultipartFile f : images) {
                if (!f.isEmpty()) {
                    String url = fileUploadService.uploadFile(f, "reports/" + report.getId());
                    report.getImages().add(ReportImage.builder().report(report).uploadedBy(citizen)
                            .imageUrl(url).beforeCleanup(true).build());
                }
            }
        }
        notificationService.notifyReportReceived(report);
        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getAll(Report.Status status, Pageable p) {
        return (status != null ? reportRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(status, p)
                : reportRepository.findByDeletedFalseOrderByCreatedAtDesc(p)).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public Page<ReportResponse> getMine(User citizen, Pageable p) {
        return reportRepository.findByCitizenAndDeletedFalseOrderByCreatedAtDesc(citizen, p).map(this::toResponse);
    }
    @Transactional(readOnly = true)
    public ReportResponse getById(Long id) { return toResponse(findActive(id)); }
    @Transactional(readOnly = true)
    public List<ReportResponse> getNearby(double lat, double lng, double r) {
        return reportRepository.findNearby(lat, lng, r).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse updateStatus(Long id, StatusUpdateRequest req) {
        Report r = findActive(id);
        r.setStatus(req.getStatus());
        if (req.getStatus() == Report.Status.RESOLVED) {
            r.setResolvedAt(LocalDateTime.now()); notificationService.notifyReportResolved(r);
        } else if (req.getStatus() == Report.Status.REJECTED) {
            notificationService.notifyReportRejected(r, req.getNotes());
        }
        return toResponse(reportRepository.save(r));
    }

    @Transactional
    public void delete(Long id) {
        Report r = findActive(id);
        r.setDeleted(true);
        r.getImages().forEach(img -> fileUploadService.deleteFile(img.getImageUrl()));
        reportRepository.save(r);
    }

    public Report findActive(Long id) {
        Report r = reportRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Report", id));
        if (r.isDeleted()) throw new ResourceNotFoundException("Report", id);
        return r;
    }

    public ReportResponse toResponse(Report r) {
        ReportResponse.AssignmentInfo ai = null;
        if (r.getAssignment() != null) {
            Assignment a = r.getAssignment();
            ai = ReportResponse.AssignmentInfo.builder().id(a.getId())
                    .collectorName(a.getCollector().getName()).status(a.getStatus().name())
                    .assignedAt(a.getAssignedAt()).build();
        }
        return ReportResponse.builder().id(r.getId()).title(r.getTitle()).description(r.getDescription())
            .category(ReportResponse.CategoryInfo.builder().id(r.getCategory().getId())
                .name(r.getCategory().getName()).icon(r.getCategory().getIcon()).build())
            .latitude(r.getLatitude()).longitude(r.getLongitude()).address(r.getAddress())
            .status(r.getStatus()).priority(r.getPriority())
            .citizen(ReportResponse.CitizenInfo.builder().id(r.getCitizen().getId())
                .name(r.getCitizen().getName()).build())
            .imageUrls(r.getImages().stream().map(ReportImage::getImageUrl).collect(Collectors.toList()))
            .assignment(ai).createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).resolvedAt(r.getResolvedAt())
            .build();
    }
}
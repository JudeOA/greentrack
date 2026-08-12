package com.greentrack.service;
import com.greentrack.dto.request.AssignmentRequest;
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

@Service @RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ReportService reportService;
    private final NotificationService notificationService;
    private final FileUploadService fileUploadService;

    @Transactional
    public ReportResponse assign(AssignmentRequest req, User admin) {
        Report report = reportService.findActive(req.getReportId());
        if (report.getStatus() == Report.Status.RESOLVED || report.getStatus() == Report.Status.REJECTED)
            throw new BusinessException("Cannot assign a " + report.getStatus() + " report");
        User collector = userRepository.findById(req.getCollectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Collector", req.getCollectorId()));
        if (collector.getRole() != User.Role.COLLECTOR) throw new BusinessException("User is not a Collector");
        if (!collector.isActive()) throw new BusinessException("Collector account is inactive");
        assignmentRepository.findByReportId(report.getId()).ifPresent(existing -> {
            assignmentRepository.delete(existing);
            assignmentRepository.flush();
        });
        assignmentRepository.save(Assignment.builder().report(report).collector(collector)
                .assignedBy(admin).notes(req.getNotes()).status(Assignment.Status.ASSIGNED).build());
        report.setStatus(Report.Status.IN_PROGRESS);
        notificationService.notifyReportAssigned(report, collector);
        return reportService.toResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getMyTasks(User collector, Pageable p) {
        return assignmentRepository.findByCollectorOrderByAssignedAtDesc(collector, p)
                .map(a -> reportService.toResponse(a.getReport()));
    }

    @Transactional
    public ReportResponse updateTaskStatus(Long assignmentId, Assignment.Status status, User collector) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
        if (!a.getCollector().getId().equals(collector.getId()))
            throw new BusinessException("You are not assigned to this task");
        if (status == Assignment.Status.COMPLETED) {
            boolean hasProof = a.getReport().getImages().stream().anyMatch(img -> !img.isBeforeCleanup());
            if (!hasProof) throw new BusinessException("Upload proof of cleanup before marking complete");
            a.setCompletedAt(LocalDateTime.now());
            a.getReport().setStatus(Report.Status.RESOLVED);
            a.getReport().setResolvedAt(LocalDateTime.now());
            notificationService.notifyReportResolved(a.getReport());
        }
        a.setStatus(status);
        assignmentRepository.save(a);
        return reportService.toResponse(a.getReport());
    }

    @Transactional
    public ReportResponse uploadProof(Long assignmentId, MultipartFile proof, User collector) {
        Assignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
        if (!a.getCollector().getId().equals(collector.getId()))
            throw new BusinessException("You are not assigned to this task");
        Report r = a.getReport();
        String url = fileUploadService.uploadFile(proof, "proofs/" + r.getId());
        r.getImages().add(ReportImage.builder().report(r).uploadedBy(collector).imageUrl(url).beforeCleanup(false).build());
        return reportService.toResponse(r);
    }
}
package com.greentrack.service;

import com.greentrack.dto.request.AssignmentRequest;
import com.greentrack.entity.*;
import com.greentrack.exception.BusinessException;
import com.greentrack.repository.AssignmentRepository;
import com.greentrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReportService reportService;
    @Mock private NotificationService notificationService;
    @Mock private FileUploadService fileUploadService;

    private AssignmentService assignmentService;

    private User admin;
    private User collector;
    private Report report;

    @BeforeEach
    void setUp() {
        assignmentService = new AssignmentService(assignmentRepository, userRepository, reportService, notificationService, fileUploadService);
        admin = User.builder().id(1L).role(User.Role.ADMIN).build();
        collector = User.builder().id(2L).role(User.Role.COLLECTOR).isActive(true).build();
        report = Report.builder().id(10L).status(Report.Status.PENDING)
                .latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).build();
    }

    // Hibernate flushes queued INSERTs before DELETEs, so deleting the old
    // assignment and inserting the new one in the same flush trips the
    // unique constraint on report_id unless the delete is flushed first.
    @Test
    void assign_reassigningExistingReport_flushesDeleteBeforeInsert() {
        Assignment existing = Assignment.builder().id(100L).report(report).collector(collector).build();
        when(reportService.findActive(10L)).thenReturn(report);
        when(userRepository.findById(2L)).thenReturn(Optional.of(collector));
        when(assignmentRepository.findByReportId(10L)).thenReturn(Optional.of(existing));
        when(reportService.toResponse(any())).thenReturn(null);

        AssignmentRequest req = new AssignmentRequest();
        req.setReportId(10L);
        req.setCollectorId(2L);

        assignmentService.assign(req, admin);

        InOrder order = inOrder(assignmentRepository);
        order.verify(assignmentRepository).delete(existing);
        order.verify(assignmentRepository).flush();
        order.verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void updateTaskStatus_completingWithoutProofImage_throws() {
        Assignment a = Assignment.builder().id(5L).collector(collector).report(report)
                .status(Assignment.Status.ASSIGNED).build();
        report.setImages(List.of());
        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> assignmentService.updateTaskStatus(5L, Assignment.Status.COMPLETED, collector))
                .isInstanceOf(BusinessException.class);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_completingWithProofImage_succeeds() {
        Assignment a = Assignment.builder().id(5L).collector(collector).report(report)
                .status(Assignment.Status.ASSIGNED).build();
        ReportImage proof = ReportImage.builder().imageUrl("/uploads/proofs/10/x.jpg").beforeCleanup(false).build();
        report.setImages(List.of(proof));
        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(a));
        when(reportService.toResponse(any())).thenReturn(null);

        assignmentService.updateTaskStatus(5L, Assignment.Status.COMPLETED, collector);

        assertThat(a.getStatus()).isEqualTo(Assignment.Status.COMPLETED);
        assertThat(report.getStatus()).isEqualTo(Report.Status.RESOLVED);
        verify(assignmentRepository).save(a);
    }

    @Test
    void updateTaskStatus_wrongCollector_throws() {
        User otherCollector = User.builder().id(99L).role(User.Role.COLLECTOR).build();
        Assignment a = Assignment.builder().id(5L).collector(otherCollector).report(report)
                .status(Assignment.Status.ASSIGNED).build();
        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> assignmentService.updateTaskStatus(5L, Assignment.Status.EN_ROUTE, collector))
                .isInstanceOf(BusinessException.class);
    }
}

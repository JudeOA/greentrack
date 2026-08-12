package com.greentrack.controller;
import com.greentrack.dto.request.AssignmentRequest;
import com.greentrack.dto.request.TaskStatusUpdateRequest;
import com.greentrack.dto.response.*;
import com.greentrack.entity.*;
import com.greentrack.service.AssignmentService;
import com.greentrack.web.PageRequestUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/assignments") @RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> assign(
            @Valid @RequestBody AssignmentRequest req, @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.ok("Assigned", assignmentService.assign(req, admin)));
    }
    @GetMapping("/mine")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> mine(
            @AuthenticationPrincipal User collector,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getMyTasks(collector, PageRequestUtil.of(page, size))));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<ApiResponse<ReportResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest req, @AuthenticationPrincipal User collector) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", assignmentService.updateTaskStatus(id, req.getStatus(), collector)));
    }
    @PostMapping(value="/{id}/proof", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<ApiResponse<ReportResponse>> proof(
            @PathVariable Long id, @RequestPart("proofImage") MultipartFile img,
            @AuthenticationPrincipal User collector) {
        return ResponseEntity.ok(ApiResponse.ok("Proof uploaded", assignmentService.uploadProof(id, img, collector)));
    }
}
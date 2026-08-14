package com.greentrack.controller;
import com.greentrack.dto.request.*;
import com.greentrack.dto.response.*;
import com.greentrack.entity.*;
import com.greentrack.service.AiClassificationService;
import com.greentrack.service.ReportService;
import com.greentrack.web.PageRequestUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final AiClassificationService aiClassificationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<ReportResponse>> create(
            @RequestPart("data") @Valid ReportRequest req,
            @RequestPart(value="images",required=false) List<MultipartFile> images,
            @AuthenticationPrincipal User citizen) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Report submitted", reportService.create(req, images, citizen)));
    }

    // AI: classify a photo into one of the existing categories (suggestion only)
    @PostMapping(value="/classify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<ClassificationResponse>> classify(
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.ok(aiClassificationService.classify(image)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getAll(
            @RequestParam(required=false) Report.Status status,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getAll(status, PageRequestUtil.of(page, size, Sort.by("createdAt").descending()))));
    }
    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getMine(
            @AuthenticationPrincipal User citizen,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMine(citizen, PageRequestUtil.of(page, size))));
    }
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> nearby(
            @RequestParam double lat, @RequestParam double lng,
            @RequestParam(defaultValue="1000") double radius) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getNearby(lat, lng, radius)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getById(id)));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", reportService.updateStatus(id, req)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Report deleted", null));
    }
}

package com.greentrack.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequest {
    @NotNull private Long reportId;
    @NotNull private Long collectorId;
    private String notes;
}
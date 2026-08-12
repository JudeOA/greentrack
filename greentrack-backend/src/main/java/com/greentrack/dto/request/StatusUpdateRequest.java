package com.greentrack.dto.request;
import com.greentrack.entity.Report;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull private Report.Status status;
    private String notes;
}
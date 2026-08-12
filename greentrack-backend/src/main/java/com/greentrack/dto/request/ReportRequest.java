package com.greentrack.dto.request;
import com.greentrack.entity.Report;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReportRequest {
    @NotBlank @Size(max = 200) private String title;
    @Size(max = 2000) private String description;
    @NotNull private Integer categoryId;
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") private BigDecimal latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") private BigDecimal longitude;
    @Size(max = 300) private String address;
    private Report.Priority priority = Report.Priority.MEDIUM;
}
package com.greentrack.repository;
import com.greentrack.entity.Report;
import java.math.BigDecimal;

public interface HeatmapProjection {
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    Report.Priority getPriority();
    Report.Status getStatus();
}

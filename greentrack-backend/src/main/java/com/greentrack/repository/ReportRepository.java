package com.greentrack.repository;
import com.greentrack.entity.Report;
import com.greentrack.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByCitizenAndDeletedFalseOrderByCreatedAtDesc(User citizen, Pageable pageable);
    Page<Report> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
    Page<Report> findByStatusAndDeletedFalseOrderByCreatedAtDesc(Report.Status status, Pageable pageable);
    long countByDeletedFalse();
    long countByStatusAndDeletedFalse(Report.Status status);

    @Query(value = "SELECT r.* FROM reports r WHERE r.is_deleted = false AND (6371000 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) <= :radius ORDER BY created_at DESC LIMIT 50", nativeQuery = true)
    List<Report> findNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);

    @Query("SELECT r.latitude as latitude, r.longitude as longitude, r.priority as priority, r.status as status " +
           "FROM Report r WHERE r.deleted = false AND r.status != 'RESOLVED'")
    List<HeatmapProjection> findActiveForHeatmap();
}
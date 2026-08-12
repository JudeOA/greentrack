package com.greentrack.repository;
import com.greentrack.entity.Assignment;
import com.greentrack.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findByReportId(Long reportId);
    Page<Assignment> findByCollectorOrderByAssignedAtDesc(User collector, Pageable pageable);
}
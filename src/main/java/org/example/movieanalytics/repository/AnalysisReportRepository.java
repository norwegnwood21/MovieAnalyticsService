package org.example.movieanalytics.repository;

import org.example.movieanalytics.entity.AnalysisReport;
import org.example.movieanalytics.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    List<AnalysisReport> findByUserOrderByCreatedAtDesc(AppUser user);
}

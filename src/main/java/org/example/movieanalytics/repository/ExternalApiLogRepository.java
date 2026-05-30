package org.example.movieanalytics.repository;

import org.example.movieanalytics.entity.ExternalApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExternalApiLogRepository extends JpaRepository<ExternalApiLog, Long> {
    @Modifying
    @Query("delete from ExternalApiLog log where log.report.id = :reportId")
    void deleteByReportId(@Param("reportId") Long reportId);
}

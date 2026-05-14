package org.example.movieanalytics.repository;

import org.example.movieanalytics.entity.ExternalApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalApiLogRepository extends JpaRepository<ExternalApiLog, Long> {}

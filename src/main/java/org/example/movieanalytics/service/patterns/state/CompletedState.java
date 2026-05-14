package org.example.movieanalytics.service.patterns.state;

import org.example.movieanalytics.entity.AnalysisReport;
import org.example.movieanalytics.entity.ReportStatus;
import java.time.LocalDateTime;

public class CompletedState implements ReportState {
    public void apply(AnalysisReport report) {
        report.setStatus(ReportStatus.COMPLETED);
        report.setFinishedAt(LocalDateTime.now());
    }
}

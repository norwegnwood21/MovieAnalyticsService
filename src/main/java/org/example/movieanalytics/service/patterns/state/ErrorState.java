package org.example.movieanalytics.service.patterns.state;

import org.example.movieanalytics.entity.AnalysisReport;
import org.example.movieanalytics.entity.ReportStatus;
import java.time.LocalDateTime;

public class ErrorState implements ReportState {
    private final String message;
    public ErrorState(String message) { this.message = message; }
    public void apply(AnalysisReport report) {
        report.setStatus(ReportStatus.ERROR);
        report.setErrorMessage(message);
        report.setFinishedAt(LocalDateTime.now());
    }
}

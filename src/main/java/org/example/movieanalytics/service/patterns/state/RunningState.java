package org.example.movieanalytics.service.patterns.state;

import org.example.movieanalytics.entity.AnalysisReport;
import org.example.movieanalytics.entity.ReportStatus;

public class RunningState implements ReportState {
    public void apply(AnalysisReport report) {
        report.setStatus(ReportStatus.RUNNING);
        report.setErrorMessage(null);
    }
}

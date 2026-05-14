package org.example.movieanalytics.service.patterns.state;

import org.example.movieanalytics.entity.AnalysisReport;
import org.springframework.stereotype.Component;

// Context из паттерна State.
@Component
public class ReportStatusContext {
    private ReportState state;
    public void setState(ReportState state) { this.state = state; }
    public void apply(AnalysisReport report) {
        if (state == null) throw new IllegalStateException("ReportState не задан");
        state.apply(report);
    }
}

package org.example.movieanalytics.service.patterns.state;

import org.example.movieanalytics.entity.AnalysisReport;

// State interface.
public interface ReportState {
    void apply(AnalysisReport report);
}

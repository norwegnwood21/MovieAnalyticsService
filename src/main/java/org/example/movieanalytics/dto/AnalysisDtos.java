package org.example.movieanalytics.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalysisDtos {
    public record CreateReportRequest(List<String> titles) {}
    public record CandidateDto(int tmdbId, String title, String releaseDate, String overview) {}
    public record MovieMatchDto(String originalTitle, boolean success, Integer selectedTmdbId, String matchedTitle,
                                String releaseDate, List<CandidateDto> candidates, String errorMessage) {}
    public record ReportResponse(Long id, String status, String errorMessage, LocalDateTime createdAt,
                                 LocalDateTime finishedAt, List<MovieMatchDto> movies,
                                 Map<String, Integer> genreStats, Map<String, Integer> yearStats,
                                 Map<String, Integer> castStats) {}
}

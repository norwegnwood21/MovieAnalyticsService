package org.example.movieanalytics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.movieanalytics.dto.AnalysisDtos;
import org.example.movieanalytics.entity.AnalysisReport;
import org.example.movieanalytics.entity.InputMovie;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReportMapper {
    private final JsonService jsonService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportMapper(JsonService jsonService) { this.jsonService = jsonService; }

    public AnalysisDtos.ReportResponse toResponse(AnalysisReport report) {
        List<AnalysisDtos.MovieMatchDto> movies = new ArrayList<>();
        for (InputMovie input : report.getInputMovies()) {
            movies.add(new AnalysisDtos.MovieMatchDto(
                    input.getOriginalTitle(), input.isSuccess(), input.getSelectedTmdbId(), input.getMatchedTitle(),
                    input.getReleaseDate(), parseCandidates(input.getCandidatesJson()), input.getErrorMessage()
            ));
        }
        return new AnalysisDtos.ReportResponse(
                report.getId(), report.getStatus().name(), report.getErrorMessage(), report.getCreatedAt(), report.getFinishedAt(),
                movies, jsonService.mapFromJson(report.getGenreStatsJson()), jsonService.mapFromJson(report.getYearStatsJson()),
                jsonService.mapFromJson(report.getCastStatsJson())
        );
    }

    private List<AnalysisDtos.CandidateDto> parseCandidates(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<List<AnalysisDtos.CandidateDto>>() {}); }
        catch (Exception e) { return List.of(); }
    }
}

package org.example.movieanalytics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
                    input.getOriginalTitle(),
                    input.isSuccess(),
                    input.getSelectedTmdbId(),
                    input.getMatchedTitle(),
                    input.getReleaseDate(),
                    parseCandidates(input.getCandidatesJson()),
                    input.getErrorMessage(),
                    parseMovieSummary(input.getTmdbDetailsJson())
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

    private AnalysisDtos.MovieSummaryDto parseMovieSummary(String rawDetailsJson) {
        if (rawDetailsJson == null || rawDetailsJson.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(rawDetailsJson);

            String title = root.path("title").asText("");
            String releaseDate = root.path("release_date").asText("");
            String year = releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : "неизвестно";
            String overview = root.path("overview").asText("");
            Integer runtime = root.hasNonNull("runtime") ? root.path("runtime").asInt() : null;
            Double voteAverage = root.hasNonNull("vote_average") ? root.path("vote_average").asDouble() : null;

            List<String> genres = new ArrayList<>();
            for (JsonNode g : root.path("genres")) {
                String name = g.path("name").asText("");
                if (!name.isBlank()) genres.add(name);
            }

            List<String> actors = new ArrayList<>();
            JsonNode castNode = root.path("credits").path("cast");
            for (int i = 0; i < Math.min(castNode.size(), 5); i++) {
                String name = castNode.get(i).path("name").asText("");
                if (!name.isBlank()) actors.add(name);
            }

            List<String> countries = new ArrayList<>();
            for (JsonNode c : root.path("production_countries")) {
                String name = c.path("name").asText("");
                if (!name.isBlank()) countries.add(name);
            }

            return new AnalysisDtos.MovieSummaryDto(title, year, genres, actors, overview, runtime, voteAverage, countries);
        } catch (Exception e) {
            return null;
        }
    }
}

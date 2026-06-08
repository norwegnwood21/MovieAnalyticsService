package org.example.movieanalytics.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnalysisDtos {
    public record CreateReportRequest(List<String> titles) {}
    public record CandidateDto(int tmdbId,
                               String title,
                               String releaseDate,
                               String overview) {}

    public record MovieSummaryDto(
            String title,
            String year,
            List<String> genres,
            List<String> actors,
            String overview,
            Integer runtime,
            Double voteAverage,
            List<String> countries
    ) {}

    public record MovieMatchDto(
            String originalTitle,      // то, что ввёл пользователь
            boolean success,           // удалось ли сопоставить фильм
            Integer selectedTmdbId,    // какой фильм выбран из TMDB
            String matchedTitle,       // название, которое вернул TMDB
            String releaseDate,        // дата выхода
            List<CandidateDto> candidates,  // все найденные варианты
            String errorMessage,       // если success == false — причина
            MovieSummaryDto summary    // Детальная информация
    ) {}
    public record ReportResponse(
            Long id,                    // ID отчёта в БД
            String status,              // RUNNING / COMPLETED / ERROR
            String errorMessage,        // если ERROR — текст ошибки
            LocalDateTime createdAt,    // когда создан отчёт
            LocalDateTime finishedAt,   // когда завершился (или null)
            List<MovieMatchDto> movies, // список сопоставленных фильмов
            Map<String, Integer> genreStats,   // жанр и количество
            Map<String, Integer> yearStats,    // год и количество
            Map<String, Integer> castStats     // актёр и количество
    ) {}
}

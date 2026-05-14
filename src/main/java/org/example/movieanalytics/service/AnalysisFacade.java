package org.example.movieanalytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.movieanalytics.dto.AnalysisDtos;
import org.example.movieanalytics.entity.*;
import org.example.movieanalytics.exception.AppException;
import org.example.movieanalytics.repository.AnalysisReportRepository;
import org.example.movieanalytics.service.patterns.builder.*;
import org.example.movieanalytics.service.patterns.state.*;
import org.example.movieanalytics.service.tmdb.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

// Facade: контроллеру не нужно знать порядок работы с БД, TMDB, состояниями и Builder.
@Service
public class AnalysisFacade {
    private final AnalysisReportRepository reportRepository;
    private final MovieDataProvider movieDataProvider;
    private final JsonService jsonService;
    private final AnalysisDirector director;
    private final DefaultAnalysisResultBuilder builder;
    private final ReportStatusContext statusContext;
    private final ReportMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisFacade(AnalysisReportRepository reportRepository, MovieDataProvider movieDataProvider, JsonService jsonService,
                          AnalysisDirector director, DefaultAnalysisResultBuilder builder,
                          ReportStatusContext statusContext, ReportMapper mapper) {
        this.reportRepository = reportRepository;
        this.movieDataProvider = movieDataProvider;
        this.jsonService = jsonService;
        this.director = director;
        this.builder = builder;
        this.statusContext = statusContext;
        this.mapper = mapper;
    }

    @Transactional
    public AnalysisDtos.ReportResponse createReport(AppUser user, List<String> titles) {
        if (titles == null || titles.isEmpty()) throw new AppException(400, "Введите хотя бы один фильм");
        List<String> cleaned = titles.stream().map(String::trim).filter(s -> !s.isBlank()).distinct().toList();
        if (cleaned.isEmpty()) throw new AppException(400, "Список фильмов пустой");

        AnalysisReport report = new AnalysisReport();
        report.setUser(user);
        statusContext.setState(new RunningState());
        statusContext.apply(report);
        for (String title : cleaned) {
            InputMovie input = new InputMovie();
            input.setOriginalTitle(title);
            report.addInputMovie(input);
        }
        report = reportRepository.save(report);
        return processReportWithTmdb(report);
    }

    private AnalysisDtos.ReportResponse processReportWithTmdb(AnalysisReport report) {
        List<TmdbMovieData> successfulMovies = new ArrayList<>();
        try {
            for (InputMovie input : report.getInputMovies()) {
                try {
                    List<TmdbCandidate> candidates = movieDataProvider.searchMovie(input.getOriginalTitle(), report);
                    List<AnalysisDtos.CandidateDto> candidateDtos = candidates.stream()
                            .map(c -> new AnalysisDtos.CandidateDto(c.tmdbId(), c.title(), c.releaseDate(), c.overview()))
                            .toList();
                    input.setCandidatesJson(jsonService.toJson(candidateDtos));

                    if (candidates.isEmpty()) {
                        input.setSuccess(false);
                        input.setErrorMessage("Фильм не найден в TMDB");
                        continue;
                    }

                    TmdbCandidate selected = candidates.get(0); // базовое сопоставление: первый результат.
                    TmdbMovieData details = movieDataProvider.getMovieDetails(selected.tmdbId(), report);
                    input.setSelectedTmdbId(details.tmdbId());
                    input.setMatchedTitle(details.title());
                    input.setReleaseDate(details.releaseDate());
                    input.setTmdbDetailsJson(details.rawJson());
                    input.setSuccess(true);
                    input.setErrorMessage(null);
                    successfulMovies.add(details);
                } catch (TmdbApiException ex) {
                    input.setSuccess(false);
                    input.setErrorMessage(ex.getMessage());
                }
            }

            if (successfulMovies.isEmpty()) {
                statusContext.setState(new ErrorState("Не удалось получить данные ни по одному фильму. Введённые данные сохранены."));
                statusContext.apply(report);
                return mapper.toResponse(reportRepository.save(report));
            }

            AnalysisResult result = director.construct(builder, successfulMovies);
            report.setGenreStatsJson(jsonService.toJson(result.getGenreStats()));
            report.setYearStatsJson(jsonService.toJson(result.getYearStats()));
            report.setCastStatsJson(jsonService.toJson(result.getCastStats()));
            statusContext.setState(new CompletedState());
            statusContext.apply(report);
        } catch (Exception ex) {
            statusContext.setState(new ErrorState("Ошибка обработки: " + ex.getMessage() + ". Введённые данные сохранены."));
            statusContext.apply(report);
        }
        return mapper.toResponse(reportRepository.save(report));
    }

    // Дополнительная функция: новый отчёт строится по сохранённым данным из БД.
    // Новых вызовов movieDataProvider / TMDB здесь нет.
    @Transactional
    public AnalysisDtos.ReportResponse repeatReportWithoutTmdb(AppUser user, Long sourceReportId) {
        AnalysisReport source = reportRepository.findById(sourceReportId)
                .orElseThrow(() -> new AppException(404, "Исходный отчёт не найден"));
        if (!source.getUser().getId().equals(user.getId())) throw new AppException(403, "Нет доступа к этому отчёту");

        AnalysisReport repeated = new AnalysisReport();
        repeated.setUser(user);
        statusContext.setState(new RunningState());
        statusContext.apply(repeated);

        List<TmdbMovieData> moviesForAnalysis = new ArrayList<>();
        for (InputMovie oldInput : source.getInputMovies()) {
            InputMovie copy = new InputMovie();
            copy.setOriginalTitle(oldInput.getOriginalTitle());
            copy.setCandidatesJson(oldInput.getCandidatesJson());
            copy.setSelectedTmdbId(oldInput.getSelectedTmdbId());
            copy.setMatchedTitle(oldInput.getMatchedTitle());
            copy.setReleaseDate(oldInput.getReleaseDate());
            copy.setTmdbDetailsJson(oldInput.getTmdbDetailsJson());
            copy.setSuccess(oldInput.isSuccess());
            copy.setErrorMessage(oldInput.getErrorMessage());
            repeated.addInputMovie(copy);

            if (oldInput.isSuccess() && oldInput.getTmdbDetailsJson() != null && !oldInput.getTmdbDetailsJson().isBlank()) {
                moviesForAnalysis.add(parseStoredMovieData(oldInput.getTmdbDetailsJson()));
            }
        }

        if (moviesForAnalysis.isEmpty()) {
            statusContext.setState(new ErrorState("Повторный анализ невозможен: в исходном отчёте нет сохранённых данных TMDB."));
            statusContext.apply(repeated);
            return mapper.toResponse(reportRepository.save(repeated));
        }

        AnalysisResult result = director.construct(builder, moviesForAnalysis);
        repeated.setGenreStatsJson(jsonService.toJson(result.getGenreStats()));
        repeated.setYearStatsJson(jsonService.toJson(result.getYearStats()));
        repeated.setCastStatsJson(jsonService.toJson(result.getCastStats()));
        statusContext.setState(new CompletedState());
        statusContext.apply(repeated);

        return mapper.toResponse(reportRepository.save(repeated));
    }

    private TmdbMovieData parseStoredMovieData(String rawJson) {
        try {
            JsonNode detailsJson = objectMapper.readTree(rawJson);

            List<String> genres = new ArrayList<>();
            for (JsonNode g : detailsJson.path("genres")) {
                String name = g.path("name").asText("");
                if (!name.isBlank()) genres.add(name);
            }

            List<String> cast = new ArrayList<>();
            JsonNode castNode = detailsJson.path("credits").path("cast");
            for (int i = 0; i < Math.min(castNode.size(), 10); i++) {
                String name = castNode.get(i).path("name").asText("");
                if (!name.isBlank()) cast.add(name);
            }

            return new TmdbMovieData(
                    detailsJson.path("id").asInt(),
                    detailsJson.path("title").asText(""),
                    detailsJson.path("release_date").asText(""),
                    detailsJson.path("overview").asText(""),
                    genres,
                    cast,
                    rawJson
            );
        } catch (Exception e) {
            throw new AppException(500, "Не удалось прочитать сохранённые данные TMDB: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AnalysisDtos.ReportResponse> history(AppUser user) {
        return reportRepository.findByUserOrderByCreatedAtDesc(user).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AnalysisDtos.ReportResponse getReport(AppUser user, Long reportId) {
        AnalysisReport report = reportRepository.findById(reportId).orElseThrow(() -> new AppException(404, "Отчёт не найден"));
        if (!report.getUser().getId().equals(user.getId())) throw new AppException(403, "Нет доступа к этому отчёту");
        return mapper.toResponse(report);
    }
}

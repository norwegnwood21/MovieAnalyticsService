package org.example.movieanalytics.service.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.movieanalytics.entity.AnalysisReport;
import org.example.movieanalytics.entity.ExternalApiLog;
import org.example.movieanalytics.repository.ExternalApiLogRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Service из паттерна Adapter: низкоуровневый клиент TMDB.
@Component
public class TmdbHttpClient {
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // ВАЖНО: для учебной проверки можно вставить сюда API Read Access Token.
    // Перед загрузкой проекта в GitHub токен обязательно удалить.
    private final String token = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJmYmY1NDBiMGM1OWU3Mzk5NDMyMGIyMzE0M2UyM2Y1NiIsIm5iZiI6MTc3ODE1MzE2OC4wMzAwMDAyLCJzdWIiOiI2OWZjNzZkMDY5OTc3MzE1MzJjNDgyNzMiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.2XGvR2T-ypa_4fbwus4kmBHGA1rR2FInQrd-dqK7mHk";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExternalApiLogRepository logRepository;

    public TmdbHttpClient(ExternalApiLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public JsonNode searchMovieRaw(String title, AnalysisReport report) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search/movie?query=" + encodedTitle + "&language=ru-RU";
        return getJson(url, "/search/movie", report);
    }

    public JsonNode movieDetailsRaw(int tmdbId, AnalysisReport report) {
        String url = BASE_URL + "/movie/" + tmdbId + "?language=ru-RU&append_to_response=credits";
        return getJson(url, "/movie/{id}", report);
    }

    private JsonNode getJson(String url, String endpoint, AnalysisReport report) {
        if (token == null || token.isBlank()) {
            throw new TmdbApiException("TMDB token не задан. Вставь API Read Access Token в TmdbHttpClient.java в поле token.");
        }

        int status = 0;
        String error = null;
        boolean success = false;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode();

            if (status == 401) throw new TmdbApiException("Ошибка авторизации TMDB: неверный API Read Access Token.");
            if (status == 404) throw new TmdbApiException("TMDB: ресурс не найден.");
            if (status == 422) throw new TmdbApiException("TMDB: неверные параметры запроса.");
            if (status == 429) throw new TmdbApiException("TMDB: превышен лимит запросов. Повтори позже.");
            if (status >= 500) throw new TmdbApiException("TMDB временно недоступен. Код: " + status);
            if (status < 200 || status >= 300) throw new TmdbApiException("TMDB вернул ошибку. Код: " + status + ", ответ: " + response.body());

            success = true;
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            error = "Сетевой сбой при обращении к TMDB: " + e.getMessage();
            throw new TmdbApiException(error);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error = "Запрос к TMDB был прерван.";
            throw new TmdbApiException(error);
        } finally {
            ExternalApiLog log = new ExternalApiLog();
            log.setReport(report);
            log.setEndpoint(endpoint);
            log.setRequestUrl(url.replaceAll("Authorization=.*", "Authorization=hidden"));
            log.setHttpStatus(status);
            log.setSuccess(success);
            log.setErrorMessage(error);
            logRepository.save(log);
        }
    }

    public List<TmdbCandidate> parseCandidates(JsonNode searchJson) {
        List<TmdbCandidate> candidates = new ArrayList<>();
        JsonNode results = searchJson.get("results");
        if (results == null || !results.isArray()) return candidates;
        for (int i = 0; i < Math.min(results.size(), 5); i++) {
            JsonNode item = results.get(i);
            candidates.add(new TmdbCandidate(
                    item.path("id").asInt(),
                    item.path("title").asText(""),
                    item.path("release_date").asText(""),
                    item.path("overview").asText("")
            ));
        }
        return candidates;
    }

    public TmdbMovieData parseMovieDetails(JsonNode detailsJson) {
        List<String> genres = new ArrayList<>();
        for (JsonNode g : detailsJson.path("genres")) genres.add(g.path("name").asText());

        List<String> cast = new ArrayList<>();
        JsonNode castNode = detailsJson.path("credits").path("cast");
        for (int i = 0; i < Math.min(castNode.size(), 10); i++) cast.add(castNode.get(i).path("name").asText());

        return new TmdbMovieData(
                detailsJson.path("id").asInt(),
                detailsJson.path("title").asText(""),
                detailsJson.path("release_date").asText(""),
                detailsJson.path("overview").asText(""),
                genres,
                cast,
                detailsJson.toString()
        );
    }
}

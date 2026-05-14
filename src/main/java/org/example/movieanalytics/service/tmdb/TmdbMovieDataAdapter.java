package org.example.movieanalytics.service.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.movieanalytics.entity.AnalysisReport;
import org.springframework.stereotype.Component;
import java.util.List;

// Adapter: реализует понятный приложению интерфейс MovieDataProvider,
// но внутри вызывает сторонний TMDB HTTP client.
@Component
public class TmdbMovieDataAdapter implements MovieDataProvider {
    private final TmdbHttpClient tmdbHttpClient;

    public TmdbMovieDataAdapter(TmdbHttpClient tmdbHttpClient) {
        this.tmdbHttpClient = tmdbHttpClient;
    }

    @Override
    public List<TmdbCandidate> searchMovie(String title, AnalysisReport report) {
        JsonNode raw = tmdbHttpClient.searchMovieRaw(title, report);
        return tmdbHttpClient.parseCandidates(raw);
    }

    @Override
    public TmdbMovieData getMovieDetails(int tmdbId, AnalysisReport report) {
        JsonNode raw = tmdbHttpClient.movieDetailsRaw(tmdbId, report);
        return tmdbHttpClient.parseMovieDetails(raw);
    }
}

package org.example.movieanalytics.service.tmdb;

import org.example.movieanalytics.entity.AnalysisReport;
import java.util.List;

// Client Interface для паттерна Adapter.
public interface MovieDataProvider {
    List<TmdbCandidate> searchMovie(String title, AnalysisReport report);
    TmdbMovieData getMovieDetails(int tmdbId, AnalysisReport report);
}

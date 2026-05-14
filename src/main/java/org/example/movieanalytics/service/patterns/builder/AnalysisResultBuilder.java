package org.example.movieanalytics.service.patterns.builder;

import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import java.util.List;

// Builder interface.
public interface AnalysisResultBuilder {
    void createResult();
    void buildGenreStats(List<TmdbMovieData> movies);
    void buildYearStats(List<TmdbMovieData> movies);
    void buildCastStats(List<TmdbMovieData> movies);
    AnalysisResult getResult();
}

package org.example.movieanalytics.service.analysis;

import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import java.util.List;
import java.util.Map;

// Strategy interface.
public interface AnalysisStrategy {
    String name();
    Map<String, Integer> analyze(List<TmdbMovieData> movies);
}

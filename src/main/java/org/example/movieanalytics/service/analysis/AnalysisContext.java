package org.example.movieanalytics.service.analysis;

import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import java.util.List;
import java.util.Map;

// Context из паттерна Strategy: хранит стратегию и делегирует ей анализ.
public class AnalysisContext {
    private AnalysisStrategy strategy;

    public AnalysisContext(AnalysisStrategy strategy) {
        this.strategy = strategy;
    }
    public void setStrategy(AnalysisStrategy strategy) {
        this.strategy = strategy;
    }
    public Map<String, Integer> execute(List<TmdbMovieData> movies) {
        return strategy.analyze(movies);
    }
}

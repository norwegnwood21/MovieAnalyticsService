package org.example.movieanalytics.service.patterns.builder;

import org.example.movieanalytics.service.analysis.*;
import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import org.springframework.stereotype.Component;
import java.util.List;

// ConcreteBuilder из паттерна Builder.
@Component
public class DefaultAnalysisResultBuilder implements AnalysisResultBuilder {
    private AnalysisResult result;
    private final GenreAnalysisStrategy genreStrategy;
    private final YearAnalysisStrategy yearStrategy;
    private final CastAnalysisStrategy castStrategy;

    public DefaultAnalysisResultBuilder(GenreAnalysisStrategy genreStrategy, YearAnalysisStrategy yearStrategy, CastAnalysisStrategy castStrategy) {
        this.genreStrategy = genreStrategy;
        this.yearStrategy = yearStrategy;
        this.castStrategy = castStrategy;
    }

    public void createResult() { result = new AnalysisResult(); }
    public void buildGenreStats(List<TmdbMovieData> movies) { result.setGenreStats(new AnalysisContext(genreStrategy).execute(movies)); }
    public void buildYearStats(List<TmdbMovieData> movies) { result.setYearStats(new AnalysisContext(yearStrategy).execute(movies)); }
    public void buildCastStats(List<TmdbMovieData> movies) { result.setCastStats(new AnalysisContext(castStrategy).execute(movies)); }
    public AnalysisResult getResult() { return result; }
}

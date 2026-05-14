package org.example.movieanalytics.service.patterns.builder;

import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import org.springframework.stereotype.Component;
import java.util.List;

// Director из паттерна Builder: именно он задает порядок шагов создания сложного результата.
@Component
public class AnalysisDirector {
    public AnalysisResult construct(AnalysisResultBuilder builder, List<TmdbMovieData> movies) {
        builder.createResult();
        builder.buildGenreStats(movies);
        builder.buildYearStats(movies);
        builder.buildCastStats(movies);
        return builder.getResult();
    }
}

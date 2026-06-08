package org.example.movieanalytics.service.analysis;

import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class YearAnalysisStrategy implements AnalysisStrategy {
    public String name() { return "years"; }
    public Map<String, Integer> analyze(List<TmdbMovieData> movies) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (TmdbMovieData movie : movies) {
            String year = "unknown";
            if (movie.releaseDate() != null && movie.releaseDate().length() >= 4) year = movie.releaseDate().substring(0, 4);
            result.merge(year, 1, Integer::sum);
        }
        return result;
    }
}

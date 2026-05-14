package org.example.movieanalytics.service.analysis;

import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CastAnalysisStrategy implements AnalysisStrategy {
    public String name() { return "cast"; }
    public Map<String, Integer> analyze(List<TmdbMovieData> movies) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (TmdbMovieData movie : movies) {
            for (String actor : movie.cast()) result.merge(actor, 1, Integer::sum);
        }
        return result;
    }
}

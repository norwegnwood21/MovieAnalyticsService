package org.example.movieanalytics.service.patterns.builder;

import java.util.LinkedHashMap;
import java.util.Map;

// Product из паттерна Builder.
public class AnalysisResult {
    private Map<String, Integer> genreStats = new LinkedHashMap<>();
    private Map<String, Integer> yearStats = new LinkedHashMap<>();
    private Map<String, Integer> castStats = new LinkedHashMap<>();

    public Map<String, Integer> getGenreStats() { return genreStats; }
    public void setGenreStats(Map<String, Integer> genreStats) { this.genreStats = genreStats; }
    public Map<String, Integer> getYearStats() { return yearStats; }
    public void setYearStats(Map<String, Integer> yearStats) { this.yearStats = yearStats; }
    public Map<String, Integer> getCastStats() { return castStats; }
    public void setCastStats(Map<String, Integer> castStats) { this.castStats = castStats; }
}

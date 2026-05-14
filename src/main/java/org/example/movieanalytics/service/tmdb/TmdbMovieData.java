package org.example.movieanalytics.service.tmdb;

import java.util.List;

public record TmdbMovieData(
        int tmdbId,
        String title,
        String releaseDate,
        String overview,
        List<String> genres,
        List<String> cast,
        String rawJson
) {}

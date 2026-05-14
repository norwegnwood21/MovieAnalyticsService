package org.example.movieanalytics.service.tmdb;

public class TmdbApiException extends RuntimeException {
    public TmdbApiException(String message) { super(message); }
}

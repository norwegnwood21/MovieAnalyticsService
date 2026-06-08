package org.example.movieanalytics.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.movieanalytics.service.tmdb.TmdbCandidate;
import org.example.movieanalytics.service.tmdb.TmdbHttpClient;
import org.example.movieanalytics.service.tmdb.TmdbMovieData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TmdbHttpClientParsingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldParseSearchCandidates() throws Exception {
        String json = """
                {
                  "page": 1,
                  "results": [
                    {
                      "id": 603,
                      "title": "Матрица",
                      "release_date": "1999-03-31",
                      "overview": "Описание фильма"
                    },
                    {
                      "id": 604,
                      "title": "Матрица: Перезагрузка",
                      "release_date": "2003-05-15",
                      "overview": "Описание продолжения"
                    }
                  ]
                }
                """;

        TmdbHttpClient client = new TmdbHttpClient(null);
        JsonNode node = objectMapper.readTree(json);

        List<TmdbCandidate> candidates = client.parseCandidates(node);

        assertEquals(2, candidates.size());
        assertEquals(603, candidates.get(0).tmdbId());
        assertEquals("Матрица", candidates.get(0).title());
        assertEquals("1999-03-31", candidates.get(0).releaseDate());
    }

    @Test
    void shouldLimitSearchCandidatesToFiveItems() throws Exception {
        String json = """
                {
                  "results": [
                    {"id": 1, "title": "A", "release_date": "2001-01-01", "overview": ""},
                    {"id": 2, "title": "B", "release_date": "2002-01-01", "overview": ""},
                    {"id": 3, "title": "C", "release_date": "2003-01-01", "overview": ""},
                    {"id": 4, "title": "D", "release_date": "2004-01-01", "overview": ""},
                    {"id": 5, "title": "E", "release_date": "2005-01-01", "overview": ""},
                    {"id": 6, "title": "F", "release_date": "2006-01-01", "overview": ""}
                  ]
                }
                """;

        TmdbHttpClient client = new TmdbHttpClient(null);
        JsonNode node = objectMapper.readTree(json);

        List<TmdbCandidate> candidates = client.parseCandidates(node);

        assertEquals(5, candidates.size());
        assertEquals(5, candidates.get(4).tmdbId());
    }

    @Test
    void shouldParseMovieDetailsWithGenresAndCast() throws Exception {
        String json = """
                {
                  "id": 27205,
                  "title": "Начало",
                  "release_date": "2010-07-15",
                  "overview": "Фильм о снах",
                  "genres": [
                    {"id": 28, "name": "боевик"},
                    {"id": 878, "name": "фантастика"}
                  ],
                  "credits": {
                    "cast": [
                      {"id": 1, "name": "Леонардо ДиКаприо"},
                      {"id": 2, "name": "Джозеф Гордон-Левитт"},
                      {"id": 3, "name": "Эллиот Пейдж"}
                    ]
                  }
                }
                """;

        TmdbHttpClient client = new TmdbHttpClient(null);
        JsonNode node = objectMapper.readTree(json);

        TmdbMovieData movie = client.parseMovieDetails(node);

        assertEquals(27205, movie.tmdbId());
        assertEquals("Начало", movie.title());
        assertEquals("2010-07-15", movie.releaseDate());
        assertTrue(movie.genres().contains("боевик"));
        assertTrue(movie.genres().contains("фантастика"));
        assertTrue(movie.cast().contains("Леонардо ДиКаприо"));
        assertEquals(3, movie.cast().size());
    }
}

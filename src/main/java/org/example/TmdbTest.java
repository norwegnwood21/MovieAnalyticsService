package org.example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TmdbTest {

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        String token ="";
        if (token == null || token.isBlank()) {
            System.out.println("Ошибка: переменная окружения TMDB_ACCESS_TOKEN не задана.");
            System.out.println("Пример для macOS/Linux:");
            System.out.println("export TMDB_ACCESS_TOKEN=\"твой_токен\"");
            return;
        }

        try {
            String movieTitle = "Crash";

            System.out.println("Ищем фильм: " + movieTitle);

            JsonNode searchResult = searchMovie(movieTitle, token);

            JsonNode results = searchResult.get("results");

            if (results == null || !results.isArray() || results.isEmpty()) {
                System.out.println("Фильм не найден.");
                return;
            }

            JsonNode firstMovie = results.get(0);

            int movieId = firstMovie.get("id").asInt();
            String foundTitle = firstMovie.get("title").asText();
            String releaseDate = firstMovie.hasNonNull("release_date")
                    ? firstMovie.get("release_date").asText()
                    : "дата неизвестна";

            System.out.println();
            System.out.println("Найден первый вариант:");
            System.out.println("ID: " + movieId);
            System.out.println("Название: " + foundTitle);
            System.out.println("Дата выхода: " + releaseDate);

            System.out.println();
            System.out.println("Получаем детальную информацию...");

            JsonNode details = getMovieDetails(movieId, token);

            printMovieDetails(details);

        } catch (Exception e) {
            System.out.println("Произошла ошибка при работе с TMDB:");
            System.out.println(e.getMessage());
        }
    }

    private static JsonNode searchMovie(String title, String token)
            throws IOException, InterruptedException {

        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);

        String url = BASE_URL + "/search/movie?query=" + encodedTitle + "&language=ru-RU";

        return sendGetRequest(url, token);
    }

    private static JsonNode getMovieDetails(int movieId, String token)
            throws IOException, InterruptedException {

        String url = BASE_URL + "/movie/" + movieId + "?language=ru-RU";

        return sendGetRequest(url, token);
    }

    private static JsonNode sendGetRequest(String url, String token)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        int statusCode = response.statusCode();

        if (statusCode == 401) {
            throw new RuntimeException("Ошибка авторизации: проверь TMDB_ACCESS_TOKEN.");
        }

        if (statusCode == 404) {
            throw new RuntimeException("Ресурс не найден: " + url);
        }

        if (statusCode == 429) {
            throw new RuntimeException("Превышен лимит запросов TMDB. Нужно повторить позже.");
        }

        if (statusCode >= 500) {
            throw new RuntimeException("Ошибка на стороне TMDB. Код: " + statusCode);
        }if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException(
                    "Неуспешный ответ от TMDB. Код: " + statusCode
                            + "\nОтвет: " + response.body()
            );
        }

        return objectMapper.readTree(response.body());
    }

    private static void printMovieDetails(JsonNode details) {
        String title = details.hasNonNull("title")
                ? details.get("title").asText()
                : "без названия";

        String releaseDate = details.hasNonNull("release_date")
                ? details.get("release_date").asText()
                : "дата неизвестна";

        double voteAverage = details.hasNonNull("vote_average")
                ? details.get("vote_average").asDouble()
                : 0.0;

        int runtime = details.hasNonNull("runtime")
                ? details.get("runtime").asInt()
                : 0;

        System.out.println();
        System.out.println("Детальная информация:");
        System.out.println("Название: " + title);
        System.out.println("Дата выхода: " + releaseDate);
        System.out.println("Рейтинг TMDB: " + voteAverage);
        System.out.println("Длительность: " + runtime + " мин.");

        System.out.println("Жанры:");

        JsonNode genres = details.get("genres");

        if (genres != null && genres.isArray()) {
            for (JsonNode genre : genres) {
                System.out.println("- " + genre.get("name").asText());
            }
        } else {
            System.out.println("Жанры не найдены.");
        }
    }
}
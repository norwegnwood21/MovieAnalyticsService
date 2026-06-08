# MovieAnalyticsService

MovieAnalyticsService — веб-сервис «Киноразбор» для аналитики пользовательского списка фильмов. Пользователь вводит несколько названий фильмов, приложение сопоставляет их с фильмами из TMDB, сохраняет полученные данные в PostgreSQL и строит аналитическую сводку: краткую информацию по каждому фильму, распределение по жанрам, годам выпуска и актёрам.

Проект реализован как Spring Boot веб-приложение с REST API, Swagger UI, PostgreSQL, статическим HTML/CSS/JS интерфейсом и тестами.

---

## 1. Основной функционал

Приложение поддерживает следующие сценарии:

1. Регистрация пользователя.
2. Аутентификация пользователя.
3. Создание нового аналитического запроса.
4. Ввод списка фильмов в рамках одного запроса.
5. Поиск фильмов через TMDB API.
6. Получение детальной информации по каждому фильму.
7. Сохранение введённых названий, найденных вариантов, выбранного фильма, детальных данных TMDB и результата анализа в PostgreSQL.
8. Отображение статуса отчёта: `RUNNING`, `COMPLETED`, `ERROR`.
9. Просмотр истории отчётов пользователя.
10. Открытие ранее созданного отчёта.
11. Удаление отчёта из локальной базы данных.
12. Повторный анализ ранее сохранённого отчёта без повторного обращения к TMDB.
13. Просмотр краткой информации по каждому фильму.
14. Просмотр визуализаций в виде HTML/CSS-диаграмм.

---

## 2. Стек технологий

В проекте используется следующий стек:

- Java 24;
- Spring Boot 3.3.5;
- Spring Web;
- Spring Data JPA;
- Hibernate;
- PostgreSQL;
- pgAdmin 4;
- TMDB REST API;
- Jackson;
- Swagger / Springdoc OpenAPI;
- HTML;
- CSS;
- JavaScript;
- Maven;
- JUnit Jupiter;
- H2 для интеграционных тестов БД.

---

## 3. Архитектура приложения

Проект разделён на несколько логических слоёв:

```text
controller  — REST-контроллеры;
service     — бизнес-логика;
tmdb        — интеграция с внешним API TMDB;
repository  — доступ к базе данных;
entity      — JPA-сущности;
dto         — DTO для обмена данными между frontend и backend;
exception   — обработка ошибок;
static      — пользовательский интерфейс;
test        — unit- и integration-тесты.
```

Основной поток выполнения:

```text
MovieAnalyticsApplication
        ↓
Spring Boot запускает приложение и поднимает Tomcat
        ↓
Пользователь открывает http://localhost:8080
        ↓
Frontend вызывает REST API
        ↓
Контроллеры принимают HTTP-запросы
        ↓
Сервисы выполняют бизнес-логику
        ↓
TmdbHttpClient получает данные из TMDB
        ↓
JPA repositories сохраняют данные в PostgreSQL
        ↓
Frontend отображает отчёт и визуализации
```

---

## 4. Основные классы проекта

### 4.1. `MovieAnalyticsApplication`

Главный класс запуска Spring Boot приложения. Он инициализирует Spring-контекст, поднимает встроенный Tomcat и подключает контроллеры, сервисы, репозитории и конфигурацию приложения.

### 4.2. `AuthController`

REST-контроллер для регистрации, входа, выхода и получения текущего пользователя.

Основные endpoints:

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
```

Контроллер получает данные от frontend, вызывает `AuthService` и сохраняет идентификатор пользователя в HTTP-сессии.

### 4.3. `ReportController`

REST-контроллер для работы с аналитическими отчётами.

Основные endpoints:

```http
POST   /api/reports
GET    /api/reports
GET    /api/reports/{id}
POST   /api/reports/{id}/repeat-local
DELETE /api/reports/{id}
```

Контроллер получает запрос, определяет текущего пользователя и передаёт выполнение в `AnalysisFacade`.

### 4.4. `AuthService`

Сервис регистрации и аутентификации. Проверяет уникальность username, создаёт пользователя, хэширует пароль через `BCryptPasswordEncoder`, проверяет логин и пароль, возвращает текущего пользователя по id из сессии.

### 4.5. `AnalysisFacade`

Фасад аналитической подсистемы. Это центральный сервис, который связывает работу с БД, TMDB, статусами отчёта и аналитической обработкой.

Функции:

- создаёт новый отчёт;
- сохраняет исходные названия фильмов;
- запускает поиск и получение деталей из TMDB;
- сохраняет найденные варианты и выбранный фильм;
- сохраняет JSON-данные TMDB;
- строит агрегированную аналитику;
- возвращает историю отчётов;
- открывает конкретный отчёт;
- удаляет отчёт;
- выполняет повторный анализ без внешнего API.

### 4.6. `TmdbHttpClient`

Низкоуровневый клиент для обращения к TMDB API. Выполняет HTTP-запросы `/search/movie` и `/movie/{id}`, передаёт токен в заголовке `Authorization: Bearer ...`, обрабатывает ошибки TMDB, логирует обращения к внешнему API и парсит ответы.

Обрабатываемые ошибки:

- 401 — ошибка авторизации;
- 404 — ресурс не найден;
- 422 — неверные параметры;
- 429 — превышен лимит запросов;
- 5xx — ошибка или недоступность TMDB;
- сетевой сбой;
- прерывание запроса.

### 4.7. `TmdbMovieDataAdapter`

Адаптер между бизнес-логикой приложения и низкоуровневым `TmdbHttpClient`. Приложение работает с интерфейсом `MovieDataProvider`, поэтому бизнес-логика не зависит напрямую от конкретной реализации TMDB-клиента.

### 4.8. `ReportMapper`

Преобразует JPA-сущности в DTO, которые отправляются на frontend. Преобразует `AnalysisReport` в `ReportResponse`, `InputMovie` в `MovieMatchDto`, читает JSON-строки с кандидатами и аналитикой, формирует данные для интерфейса.

### 4.9. `JsonService`

Вспомогательный сервис для сериализации и десериализации JSON. Используется для хранения списка кандидатов TMDB, статистики по жанрам, годам и актёрам.

---

## 5. Сущности базы данных

### 5.1. `AppUser`

Сущность пользователя.

Основные поля:

```text
id
username
email
passwordHash
createdAt
```

Используется для регистрации, входа и связи пользователя с его аналитическими отчётами.

### 5.2. `AnalysisReport`

Сущность аналитического отчёта.

Основные поля:

```text
id
user
status
errorMessage
genreStatsJson
yearStatsJson
castStatsJson
createdAt
completedAt
inputMovies
```

Один отчёт связан с одним пользователем и содержит список введённых фильмов.

### 5.3. `InputMovie`

Сущность фильма внутри аналитического запроса.

Основные поля:

```text
id
report
originalTitle
candidatesJson
selectedTmdbId
matchedTitle
releaseDate
tmdbDetailsJson
success
errorMessage
```

В этой таблице сохраняется как исходное название, введённое пользователем, так и результат сопоставления с TMDB.

### 5.4. `ExternalApiLog`

Сущность лога обращения к внешнему API.

Основные поля:

```text
id
report
endpoint
requestUrl
httpStatus
success
errorMessage
createdAt
```

Используется для фиксации всех обращений к TMDB.

---

## 6. Запросы к базе данных

В проекте используется Spring Data JPA, поэтому SQL-запросы чаще всего генерируются автоматически на основе методов репозиториев и операций Hibernate. Ниже описаны логические SQL-запросы, которые выполняются приложением.

### 6.1. Регистрация пользователя

При регистрации сначала проверяется, существует ли пользователь с таким username.

```sql
SELECT *
FROM app_users
WHERE username = ?;
```

Если пользователь не найден, создаётся новая запись:

```sql
INSERT INTO app_users (username, email, password_hash, created_at)
VALUES (?, ?, ?, ?);
```

Используется в:

```text
AuthService.register(...)
AppUserRepository.findByUsername(...)
AppUserRepository.save(...)
```

### 6.2. Вход пользователя

При входе приложение ищет пользователя по username.

```sql
SELECT *
FROM app_users
WHERE username = ?;
```

После этого пароль проверяется на уровне приложения через `BCryptPasswordEncoder`.

Используется в:

```text
AuthService.login(...)
AppUserRepository.findByUsername(...)
```

### 6.3. Получение текущего пользователя

При обращении к защищённым действиям приложение получает пользователя по id из сессии.

```sql
SELECT *
FROM app_users
WHERE id = ?;
```

Используется в:

```text
AuthService.requireUser(...)
AppUserRepository.findById(...)
```

### 6.4. Создание аналитического отчёта

При запуске анализа создаётся новый отчёт.

```sql
INSERT INTO analysis_reports
(user_id, status, error_message, genre_stats_json, year_stats_json, cast_stats_json, created_at, completed_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);
```

Для каждого введённого фильма создаётся запись в таблице `input_movies`.

```sql
INSERT INTO input_movies
(report_id, original_title, candidates_json, selected_tmdb_id, matched_title, release_date, tmdb_details_json, success, error_message)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
```

Используется в:

```text
AnalysisFacade.createReport(...)
AnalysisReportRepository.save(...)
```

### 6.5. Логирование обращений к TMDB

После каждого обращения к TMDB сохраняется лог.

```sql
INSERT INTO external_api_logs
(report_id, endpoint, request_url, http_status, success, error_message, created_at)
VALUES (?, ?, ?, ?, ?, ?, ?);
```

Используется в:

```text
TmdbHttpClient.getJson(...)
ExternalApiLogRepository.save(...)
```

### 6.6. Обновление отчёта после анализа

После получения данных TMDB и построения аналитики обновляется статус отчёта и JSON-поля со статистикой.

```sql
UPDATE analysis_reports
SET status = ?,
    error_message = ?,
    genre_stats_json = ?,
    year_stats_json = ?,
    cast_stats_json = ?,
    completed_at = ?
WHERE id = ?;
```

Также обновляются данные каждого фильма внутри отчёта:

```sql
UPDATE input_movies
SET candidates_json = ?,
    selected_tmdb_id = ?,
    matched_title = ?,
    release_date = ?,
    tmdb_details_json = ?,
    success = ?,
    error_message = ?
WHERE id = ?;
```

Используется в:

```text
AnalysisFacade.processReportWithTmdb(...)
AnalysisReportRepository.save(...)
```

### 6.7. Просмотр истории отчётов

Для отображения истории загружаются все отчёты текущего пользователя.

```sql
SELECT *
FROM analysis_reports
WHERE user_id = ?
ORDER BY created_at DESC;
```

При отображении отчёта дополнительно загружается список связанных фильмов:

```sql
SELECT *
FROM input_movies
WHERE report_id = ?;
```

Используется в:

```text
AnalysisFacade.history(...)
AnalysisReportRepository.findByUserOrderByCreatedAtDesc(...)
```

### 6.8. Открытие конкретного отчёта

При открытии отчёта по id выполняется поиск отчёта.

```sql
SELECT *
FROM analysis_reports
WHERE id = ?;
```

Затем приложение проверяет, принадлежит ли отчёт текущему пользователю.

Используется в:

```text
AnalysisFacade.getReport(...)
AnalysisReportRepository.findById(...)
```

### 6.9. Повторный анализ без обращения к TMDB

При повторном анализе приложение загружает старый отчёт и использует сохранённые `tmdbDetailsJson`.

```sql
SELECT *
FROM analysis_reports
WHERE id = ?;
```

```sql
SELECT *
FROM input_movies
WHERE report_id = ?;
```

После этого создаётся новый отчёт:

```sql
INSERT INTO analysis_reports
(user_id, status, error_message, genre_stats_json, year_stats_json, cast_stats_json, created_at, completed_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);
```

И копируются связанные фильмы:

```sql
INSERT INTO input_movies
(report_id, original_title, candidates_json, selected_tmdb_id, matched_title, release_date, tmdb_details_json, success, error_message)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
```

Внешние запросы к TMDB при этом не выполняются.

Используется в:

```text
AnalysisFacade.repeatReportWithoutTmdb(...)
```

### 6.10. Удаление отчёта

При удалении отчёта сначала удаляются логи внешнего API, связанные с отчётом.

```sql
DELETE FROM external_api_logs
WHERE report_id = ?;
```

Затем удаляется сам отчёт:

```sql
DELETE FROM analysis_reports
WHERE id = ?;
```

Связанные `input_movies` удаляются каскадно через связь `AnalysisReport` → `InputMovie`. Логически это соответствует:

```sql
DELETE FROM input_movies
WHERE report_id = ?;
```

Используется в:

```text
AnalysisFacade.deleteReport(...)
ExternalApiLogRepository.deleteByReport(...)
AnalysisReportRepository.delete(...)
```

---

## 7. Интеграция с TMDB

Для получения данных о фильмах используется официальный REST API TMDB.

### 7.1. Поиск фильма

```http
GET https://api.themoviedb.org/3/search/movie?query={title}&language=ru-RU
```

Используется для поиска фильма по названию, введённому пользователем.

### 7.2. Детальная информация о фильме

```http
GET https://api.themoviedb.org/3/movie/{tmdbId}?language=ru-RU&append_to_response=credits
```

Используется для получения названия фильма, даты выхода, описания, жанров, актёров и сырого JSON-ответа TMDB.

---

## 8. Обработка ошибок

В `TmdbHttpClient` обрабатываются:

```text
401 — неверный API Read Access Token;
404 — ресурс не найден;
422 — неверные параметры запроса;
429 — превышен лимит запросов;
5xx — временная недоступность TMDB;
IOException — сетевой сбой;
InterruptedException — прерывание запроса.
```

Если один фильм не удалось получить, введённые пользователем данные не теряются. Ошибка сохраняется в конкретном `InputMovie`, а отчёт продолжает обрабатывать остальные фильмы.

`GlobalExceptionHandler` преобразует исключения приложения в понятные JSON-ответы для frontend.

Пример ответа:

```json
{
  "error": "Отчёт не найден"
}
```

---

## 9. Использованные паттерны проектирования

### 9.1. Facade / Фасад

Класс `AnalysisFacade` скрывает от контроллера сложную подсистему: работу с БД, обращения к TMDB, обработку ошибок, обновление статусов, повторный анализ, удаление отчётов и формирование DTO.

### 9.2. Adapter / Адаптер

Классы:

```text
MovieDataProvider
TmdbMovieDataAdapter
TmdbHttpClient
```

Приложение работает с интерфейсом `MovieDataProvider`, а `TmdbMovieDataAdapter` переводит эти вызовы в конкретные HTTP-запросы к TMDB через `TmdbHttpClient`.

### 9.3. Builder / Строитель

Классы:

```text
AnalysisDirector
AnalysisResultBuilder
DefaultAnalysisResultBuilder
AnalysisResult
```

Паттерн используется для пошагового построения аналитического результата. `AnalysisDirector` управляет процессом создания результата, а `DefaultAnalysisResultBuilder` заполняет отдельные части аналитики.

### 9.4. State / Состояние

Классы:

```text
ReportStatusContext
ReportState
RunningState
CompletedState
ErrorState
```

Паттерн используется для установки состояния аналитического отчёта: `RUNNING`, `COMPLETED`, `ERROR`.

### 9.5. Strategy / Стратегия

Классы:

```text
AnalysisStrategy
GenreAnalysisStrategy
YearAnalysisStrategy
CastAnalysisStrategy
AnalysisContext
```

Паттерн используется для разделения алгоритмов аналитической обработки: анализ жанров, годов выпуска и актёров.

---

## 10. Пользовательский интерфейс

Пользовательский интерфейс расположен в:

```text
src/main/resources/static
```

Основные файлы:

```text
index.html — структура страницы;
style.css  — оформление;
app.js     — логика взаимодействия с backend.
```

UI позволяет зарегистрироваться, войти, ввести список фильмов, запустить анализ, увидеть найденные варианты TMDB, увидеть краткую информацию по каждому фильму, посмотреть визуализации, открыть историю отчётов, повторить анализ без TMDB и удалить отчёт.

Визуализации реализованы без `canvas` и без Chart.js, через HTML/CSS-диаграммы. Это сделано для стабильного отображения в разных браузерах.

---

## 11. Основные endpoints

### 11.1. Auth API

```http
POST /api/auth/register
```

Регистрация пользователя.

```http
POST /api/auth/login
```

Вход пользователя.

```http
POST /api/auth/logout
```

Выход пользователя.

```http
GET /api/auth/me
```

Получение текущего пользователя.

### 11.2. Reports API

```http
POST /api/reports
```

Создание нового аналитического отчёта.

```http
GET /api/reports
```

Получение истории отчётов текущего пользователя.

```http
GET /api/reports/{id}
```

Получение конкретного отчёта.

```http
POST /api/reports/{id}/repeat-local
```

Повторный анализ сохранённого отчёта без обращения к TMDB.

```http
DELETE /api/reports/{id}
```

Удаление отчёта из локальной базы данных.

---

## 12. Запуск приложения

### 12.1. Запуск из терминала

Из корня проекта:

```bash
mvn spring-boot:run
```

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

### 12.2. Запуск из IntelliJ IDEA

Открыть класс:

```text
MovieAnalyticsApplication
```

и нажать зелёную кнопку запуска.

После запуска открыть:

```text
http://localhost:8080
```

### 12.3. Swagger UI

Swagger доступен по адресу:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON доступен по адресу:

```text
http://localhost:8080/v3/api-docs
```

---

## 13. Тестирование

В проекте используются 3 unit-теста и 2 интеграционных теста.

Тесты не используют `spring-boot-starter-test`, чтобы не подтягивать Mockito. Вместо этого используются JUnit Jupiter и H2.

### 13.1. `TmdbHttpClientParsingTest`

Тип: unit-тесты.

Содержит 3 проверки:

```text
shouldParseSearchCandidates
shouldLimitSearchCandidatesToFiveItems
shouldParseMovieDetailsWithGenresAndCast
```

Проверяется корректный парсинг результатов поиска TMDB, ограничение списка кандидатов до 5 элементов, парсинг детальной информации, извлечение жанров, актёров и сохранение сырого JSON.

Тесты не выполняют реальные HTTP-запросы к TMDB. Они используют заранее подготовленный JSON.

### 13.2. `UserTableJdbcIntegrationTest`

Тип: интеграционный тест БД.

Проверяется создание временной H2-базы, создание таблицы пользователей, вставка пользователя через SQL `INSERT`, поиск пользователя через SQL `SELECT`, проверка сохранённого username/email.

### 13.3. `ReportTableJdbcIntegrationTest`

Тип: интеграционный тест БД.

Проверяется создание временной H2-базы, создание таблицы отчётов, вставка отчёта через SQL `INSERT`, поиск отчёта через SQL `SELECT`, удаление отчёта через SQL `DELETE`, проверка, что запись действительно удалена.

### 13.4. Запуск всех тестов

```bash
mvn clean test
```

### 13.5. Запуск только unit-тестов TMDB

```bash
mvn -Dtest=TmdbHttpClientParsingTest test
```

### 13.6. Запуск только интеграционных тестов БД

```bash
mvn -Dtest=*IntegrationTest test
```

### 13.7. Запуск конкретного интеграционного теста

```bash
mvn -Dtest=UserTableJdbcIntegrationTest test
```

или:

```bash
mvn -Dtest=ReportTableJdbcIntegrationTest test
```

---

## 14. Структура проекта

```text
MovieAnalyticsService
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── org/example/movieanalytics
│   │   │       ├── MovieAnalyticsApplication.java
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       ├── entity
│   │   │       ├── exception
│   │   │       ├── repository
│   │   │       └── service
│   │   └── resources
│   │       ├── application.properties
│   │       └── static
│   │           ├── index.html
│   │           ├── style.css
│   │           └── app.js
│   └── test
│       └── java
│           └── org/example/movieanalytics
│               ├── integration
│               │   ├── ReportTableJdbcIntegrationTest.java
│               │   └── UserTableJdbcIntegrationTest.java
│               └── tmdb
│                   └── TmdbHttpClientParsingTest.java
```

---

## 15. Итог

Проект MovieAnalyticsService позволяет пользователю получить структурированный разбор списка фильмов. Приложение интегрируется с TMDB, сохраняет данные в PostgreSQL, отображает историю отчётов и визуализации, поддерживает повторный анализ без внешнего API и удаление отчётов. Архитектура проекта разделяет пользовательский интерфейс, REST API, бизнес-логику, работу с внешним API и слой хранения данных.

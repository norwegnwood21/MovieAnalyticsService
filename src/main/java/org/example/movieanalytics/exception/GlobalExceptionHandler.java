package org.example.movieanalytics.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleApp(AppException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of(
                "time", LocalDateTime.now().toString(),
                "error", e.getMessage()
        ));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception e) {
        return ResponseEntity.status(500).body(Map.of(
                "time", LocalDateTime.now().toString(),
                "error", "Внутренняя ошибка приложения: " + e.getMessage()
        ));
    }
}

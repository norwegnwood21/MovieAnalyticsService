package org.example.movieanalytics.dto;

public class AuthDtos {
    public record RegisterRequest(String username, String email, String password) {}
    public record LoginRequest(String username, String password) {}
    public record UserResponse(Long id, String username, String email) {}
}

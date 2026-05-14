package org.example.movieanalytics.controller;

import jakarta.servlet.http.HttpSession;
import org.example.movieanalytics.dto.AuthDtos;
import org.example.movieanalytics.entity.AppUser;
import org.example.movieanalytics.exception.AppException;
import org.example.movieanalytics.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String USER_ID = "USER_ID";
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public AuthDtos.UserResponse register(@RequestBody AuthDtos.RegisterRequest request, HttpSession session) {
        AppUser user = authService.register(request);
        session.setAttribute(USER_ID, user.getId());
        return new AuthDtos.UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @PostMapping("/login")
    public AuthDtos.UserResponse login(@RequestBody AuthDtos.LoginRequest request, HttpSession session) {
        AppUser user = authService.login(request);
        session.setAttribute(USER_ID, user.getId());
        return new AuthDtos.UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) { session.invalidate(); }

    @GetMapping("/me")
    public AuthDtos.UserResponse me(HttpSession session) {
        Long id = (Long) session.getAttribute(USER_ID);
        AppUser user = authService.requireUser(id);
        return new AuthDtos.UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    public static Long currentUserId(HttpSession session) {
        Object id = session.getAttribute(USER_ID);
        if (!(id instanceof Long userId)) throw new AppException(401, "Сначала войдите в систему");
        return userId;
    }
}

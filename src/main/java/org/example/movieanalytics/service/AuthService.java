package org.example.movieanalytics.service;

import org.example.movieanalytics.dto.AuthDtos;
import org.example.movieanalytics.entity.AppUser;
import org.example.movieanalytics.exception.AppException;
import org.example.movieanalytics.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository userRepository) { this.userRepository = userRepository; }

    public AppUser register(AuthDtos.RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) throw new AppException(400, "Введите имя пользователя");
        if (request.email() == null || request.email().isBlank()) throw new AppException(400, "Введите email");
        if (request.password() == null || request.password().length() < 4) throw new AppException(400, "Пароль должен быть не короче 4 символов");
        if (userRepository.existsByUsername(request.username())) throw new AppException(409, "Такой username уже существует");
        if (userRepository.existsByEmail(request.email())) throw new AppException(409, "Такой email уже существует");
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(encoder.encode(request.password()));
        return userRepository.save(user);
    }

    public AppUser login(AuthDtos.LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AppException(401, "Неверный логин или пароль"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException(401, "Неверный логин или пароль");
        }
        return user;
    }

    public AppUser requireUser(Long userId) {
        if (userId == null) throw new AppException(401, "Сначала войдите в систему");
        return userRepository.findById(userId).orElseThrow(() -> new AppException(401, "Пользователь не найден"));
    }
}

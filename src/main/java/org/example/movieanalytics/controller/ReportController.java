package org.example.movieanalytics.controller;

import jakarta.servlet.http.HttpSession;
import org.example.movieanalytics.dto.AnalysisDtos;
import org.example.movieanalytics.entity.AppUser;
import org.example.movieanalytics.service.AnalysisFacade;
import org.example.movieanalytics.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final AuthService authService;
    private final AnalysisFacade analysisFacade;

    public ReportController(AuthService authService, AnalysisFacade analysisFacade) {
        this.authService = authService;
        this.analysisFacade = analysisFacade;
    }

    @PostMapping
    public AnalysisDtos.ReportResponse create(@RequestBody AnalysisDtos.CreateReportRequest request, HttpSession session) {
        AppUser user = authService.requireUser(AuthController.currentUserId(session));
        return analysisFacade.createReport(user, request.titles());
    }

    @GetMapping
    public List<AnalysisDtos.ReportResponse> history(HttpSession session) {
        AppUser user = authService.requireUser(AuthController.currentUserId(session));
        return analysisFacade.history(user);
    }

    @GetMapping("/{id}")
    public AnalysisDtos.ReportResponse get(@PathVariable Long id, HttpSession session) {
        AppUser user = authService.requireUser(AuthController.currentUserId(session));
        return analysisFacade.getReport(user, id);
    }

    // Дополнительная функция: повторный анализ уже сохранённого списка без внешнего API.
    @PostMapping("/{id}/repeat-local")
    public AnalysisDtos.ReportResponse repeatLocal(@PathVariable Long id, HttpSession session) {
        AppUser user = authService.requireUser(AuthController.currentUserId(session));
        return analysisFacade.repeatReportWithoutTmdb(user, id);
    }
}

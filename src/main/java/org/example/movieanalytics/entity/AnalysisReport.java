package org.example.movieanalytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_reports")
public class AnalysisReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private AppUser user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.RUNNING;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    @Column(columnDefinition = "TEXT")
    private String genreStatsJson;
    @Column(columnDefinition = "TEXT")
    private String yearStatsJson;
    @Column(columnDefinition = "TEXT")
    private String castStatsJson;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InputMovie> inputMovies = new ArrayList<>();

    public void addInputMovie(InputMovie inputMovie) {
        inputMovies.add(inputMovie);
        inputMovie.setReport(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getGenreStatsJson() { return genreStatsJson; }
    public void setGenreStatsJson(String genreStatsJson) { this.genreStatsJson = genreStatsJson; }
    public String getYearStatsJson() { return yearStatsJson; }
    public void setYearStatsJson(String yearStatsJson) { this.yearStatsJson = yearStatsJson; }
    public String getCastStatsJson() { return castStatsJson; }
    public void setCastStatsJson(String castStatsJson) { this.castStatsJson = castStatsJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public List<InputMovie> getInputMovies() { return inputMovies; }
    public void setInputMovies(List<InputMovie> inputMovies) { this.inputMovies = inputMovies; }
}

package org.example.movieanalytics.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "input_movies")
public class InputMovie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private AnalysisReport report;
    @Column(nullable = false)
    private String originalTitle;
    private Integer selectedTmdbId;
    private String matchedTitle;
    private String releaseDate;
    @Column(columnDefinition = "TEXT")
    private String candidatesJson;
    @Column(columnDefinition = "TEXT")
    private String tmdbDetailsJson;
    private boolean success;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnalysisReport getReport() { return report; }
    public void setReport(AnalysisReport report) { this.report = report; }
    public String getOriginalTitle() { return originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }
    public Integer getSelectedTmdbId() { return selectedTmdbId; }
    public void setSelectedTmdbId(Integer selectedTmdbId) { this.selectedTmdbId = selectedTmdbId; }
    public String getMatchedTitle() { return matchedTitle; }
    public void setMatchedTitle(String matchedTitle) { this.matchedTitle = matchedTitle; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public String getCandidatesJson() { return candidatesJson; }
    public void setCandidatesJson(String candidatesJson) { this.candidatesJson = candidatesJson; }
    public String getTmdbDetailsJson() { return tmdbDetailsJson; }
    public void setTmdbDetailsJson(String tmdbDetailsJson) { this.tmdbDetailsJson = tmdbDetailsJson; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

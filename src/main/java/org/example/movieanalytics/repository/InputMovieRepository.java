package org.example.movieanalytics.repository;

import org.example.movieanalytics.entity.InputMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InputMovieRepository extends JpaRepository<InputMovie, Long> {}

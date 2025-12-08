package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.MovieShowtimesDTO;
import com.project.cinemabackend.dto.ShowtimeDTO;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.repository.ShowtimeRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;

    ShowtimeService(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }
}

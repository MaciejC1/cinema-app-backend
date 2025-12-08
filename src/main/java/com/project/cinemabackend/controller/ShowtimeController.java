package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.MovieShowtimesDTO;
import com.project.cinemabackend.dto.ShowtimeDTO;
import com.project.cinemabackend.service.ShowtimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ShowtimeController {
    ShowtimeService showtimeService;

    ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }
}

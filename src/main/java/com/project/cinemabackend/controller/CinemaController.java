package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.CinemaMinimalDTO;
import com.project.cinemabackend.service.CinemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CinemaController {
    CinemaService cinemaService;

    CinemaController(CinemaService cinemaService) {
        this.cinemaService =  cinemaService;
    }

    @GetMapping("/public/cinemas")
    public ResponseEntity<List<CinemaMinimalDTO>>  getAllCinemas() {
        return ResponseEntity.ok(cinemaService.findAll());
    }
}

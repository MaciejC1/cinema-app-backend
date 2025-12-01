package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.CinemaMinimalDTO;
import com.project.cinemabackend.mapper.CinemaMapper;
import com.project.cinemabackend.repository.CinemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CinemaService {
    private final CinemaRepository cinemaRepository;
    private final CinemaMapper  cinemaMapper;

    public CinemaService(CinemaRepository cinemaRepository, CinemaMapper cinemaMapper) {
        this.cinemaRepository = cinemaRepository;
        this.cinemaMapper = cinemaMapper;
    }

    public List<CinemaMinimalDTO>  findAll(){
        return cinemaMapper.toMinimalDtoList(cinemaRepository.findAll());
    }
}

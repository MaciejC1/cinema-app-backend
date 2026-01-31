package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.MovieShowtimesDTO;
import com.project.cinemabackend.dto.SeatShowtimeDTO;
import com.project.cinemabackend.dto.ShowtimeDTO;
import com.project.cinemabackend.dto.ShowtimeDetailsDTO;
import com.project.cinemabackend.mapper.BookingSeatMapper;
import com.project.cinemabackend.mapper.SeatMapper;
import com.project.cinemabackend.mapper.ShowtimeMapper;
import com.project.cinemabackend.model.BookingSeat;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.Seat;
import com.project.cinemabackend.model.Showtime;
import com.project.cinemabackend.repository.BookingSeatRepository;
import com.project.cinemabackend.repository.ShowtimeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeMapper showtimeMapper;
    private final SeatMapper seatMapper;
    private final BookingSeatRepository bookingSeatRepository;

    ShowtimeService(
            ShowtimeRepository showtimeRepository,
            ShowtimeMapper showtimeMapper,
            SeatMapper seatMapper,
            BookingSeatRepository bookingSeatRepository
    )
    {
        this.showtimeRepository = showtimeRepository;
        this.showtimeMapper = showtimeMapper;
        this.seatMapper = seatMapper;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public ShowtimeDetailsDTO findShowtimeById(UUID id) {
        Showtime showtime = showtimeRepository.findShowtimeById(id)
                .orElseThrow(() -> new EntityNotFoundException("Showtime not found"));

        Set<BookingSeat> bookingSeats = bookingSeatRepository.findBookingSeatsByBooking_Showtime_Id(showtime.getId());

        return showtimeMapper.toDetailsDto(showtime, bookingSeats.stream().toList(), seatMapper);
    }
}

package com.project.cinemabackend.repository;

import com.project.cinemabackend.dto.LastBookingDTO;
import com.project.cinemabackend.model.Booking;
import com.project.cinemabackend.model.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @EntityGraph(attributePaths = {
            "showtime",
            "showtime.movie",
            "showtime.hall.cinema",
            "seats.seat"
    })
    Optional<Booking> findByBookingCode(String bookingCode);


    //LastBookingDTO findBookingByBookingCode(@Param("code") String code);
}

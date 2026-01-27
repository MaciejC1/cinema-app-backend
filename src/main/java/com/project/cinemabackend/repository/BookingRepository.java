package com.project.cinemabackend.repository;

import com.project.cinemabackend.dto.LastBookingDTO;
import com.project.cinemabackend.model.Booking;
import com.project.cinemabackend.model.BookingStatus;
import com.project.cinemabackend.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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


    @EntityGraph(attributePaths = {
            "showtime",
            "showtime.movie",
            "showtime.hall.cinema",
            "seats.seat"
    })
    @Query("""
    SELECT b FROM Booking b
    WHERE b.user.id = :userId
      AND b.status = COALESCE(:status, b.status)
      AND b.createdAt >= COALESCE(:fromDate, b.createdAt)
      AND b.createdAt <= COALESCE(:toDate, b.createdAt)
      AND b.totalAmount >= COALESCE(:minAmount, b.totalAmount)
      AND b.totalAmount <= COALESCE(:maxAmount, b.totalAmount)
""")

    Page<Booking> findUserBookings(
            @Param("userId") UUID userId,
            @Param("status") BookingStatus status,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );


}

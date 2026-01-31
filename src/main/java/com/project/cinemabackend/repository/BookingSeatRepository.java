package com.project.cinemabackend.repository;

import com.project.cinemabackend.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {
    @Query("""
    SELECT bs
    FROM BookingSeat bs
    JOIN bs.booking b
    WHERE b.showtime.id = :showtimeId
""")
    Set<BookingSeat> findBookedSeats(UUID showtimeId);

    List<BookingSeat> findBookingSeatsBySeat_IdIn(List<UUID> seatIds);

}

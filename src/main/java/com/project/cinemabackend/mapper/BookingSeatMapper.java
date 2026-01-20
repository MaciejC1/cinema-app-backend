package com.project.cinemabackend.mapper;

import com.project.cinemabackend.model.BookingSeat;
import com.project.cinemabackend.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingSeatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "seat", source = "seat")
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "status", constant = "VALID")
    @Mapping(target = "ticketCode", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    BookingSeat toBookingSeat(Seat seat);

    List<BookingSeat> toBookingSeatList(List<Seat> seats);
}


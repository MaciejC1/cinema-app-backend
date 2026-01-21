package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.SeatDTO;
import com.project.cinemabackend.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper()
public interface SeatMapper {
    @Mapping(target = "rowNumber", source = "seat.rowNumber")
    SeatDTO toSeatDTO(Seat seat);
}

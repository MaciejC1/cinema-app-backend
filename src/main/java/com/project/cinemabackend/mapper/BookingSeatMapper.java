package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.BookingSeatDTO;
import com.project.cinemabackend.model.BookingSeat;
import com.project.cinemabackend.util.QrGenerator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.List;

@Mapper(uses = {SeatMapper.class, QrGenerator.class})
public interface BookingSeatMapper {

    @Mapping(target = "seat", source = "seat")
    @Mapping(target = "qrCode", expression = "java(generateQrCode(bookingSeat.getTicketCode().toString()))")
    BookingSeatDTO toBookingSeatDTO(BookingSeat bookingSeat);

    List <BookingSeatDTO> toBookingSeatDTOList(List<BookingSeat> bookingSeats);

    default byte[] generateQrCode(String code) {
        return QrGenerator.generateQr(code);
    }
}
package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.payu.BookingRequest;
import com.project.cinemabackend.dto.payu.BookingResponse;
import com.project.cinemabackend.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "id")
    @Mapping(target = "payuOrderId", source = "payment.transactionId")
    @Mapping(target = "redirectUrl", ignore = true)
    BookingResponse toBookingResponse(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "showtime", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "bookingCode", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "payment", ignore = true)
    Booking toBooking(BookingRequest request);
}


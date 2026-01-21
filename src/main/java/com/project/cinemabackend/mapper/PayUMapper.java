package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.payu.BookingRequest;
import com.project.cinemabackend.dto.payu.BookingResponse;
import com.project.cinemabackend.dto.payu.PayUOrderRequest;
import com.project.cinemabackend.model.BookingSeat;
import com.project.cinemabackend.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Mapper()
public interface PayUMapper {
    @Mapping(target = "continueUrl", ignore = true)
    @Mapping(target = "notifyUrl", ignore = true)
    @Mapping(target = "customerIp", source = "request.customerIp")
    @Mapping(target = "merchantPosId", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "currencyCode", constant = "PLN")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "extOrderId", ignore = true)
    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "products", ignore = true)
    PayUOrderRequest toPayUOrderRequest(BookingRequest request);

    @Mapping(target = "email", source = "guestEmail")
    @Mapping(target = "phone", source = "guestPhone")
    @Mapping(target = "firstName", source = "guestFirstName")
    @Mapping(target = "lastName", source = "guestLastName")
    @Mapping(target = "language", constant = "pl")
    PayUOrderRequest.Buyer toBuyer(BookingRequest request);

    @Mapping(target = "bookingId", source = "bookingId")
    @Mapping(target = "bookingCode", source = "bookingCode")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "redirectUrl", source = "redirectUrl")
    @Mapping(target = "payuOrderId", source = "payuOrderId")
    BookingResponse toBookingResponse(UUID bookingId, String bookingCode,
                                      BigDecimal totalAmount, String redirectUrl,
                                      String payuOrderId);
}

package com.project.cinemabackend.dto.payu;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class BookingResponse {

    private UUID bookingId;
    private String bookingCode;
    private BigDecimal totalAmount;
    private String redirectUrl;
    private String payuOrderId;
}

package com.project.cinemabackend.dto.payu;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BookingRequest {

    private UUID showtimeId;
    private List<UUID> seatIds;
    private String guestEmail;
    private String guestPhone;
    private String guestFirstName;
    private String guestLastName;
    private String customerIp;
}

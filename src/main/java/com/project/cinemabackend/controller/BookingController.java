package com.project.cinemabackend.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cinemabackend.dto.LastBookingDTO;
import com.project.cinemabackend.dto.payu.BookingRequest;
import com.project.cinemabackend.dto.payu.BookingResponse;
import com.project.cinemabackend.dto.payu.PayUNotification;
import com.project.cinemabackend.service.BookingService;
import com.project.cinemabackend.service.PayUService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PayUService payUService;

    public BookingController(BookingService bookingService, PayUService payUService) {
        this.bookingService = bookingService;
        this.payUService = payUService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        BookingResponse response = bookingService.createBookingWithPayment(httpRequest, request, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/payu/notify", consumes = "application/json")
    public ResponseEntity<String> handlePayUNotification(
            @RequestBody String rawBody,
            @RequestHeader(value = "OpenPayu-Signature", required = false) String signatureHeader) {

        try {
            PayUNotification notification = payUService.validateAndParsePayUNotification(signatureHeader, rawBody);

            bookingService.handlePaymentNotification(notification);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing notification");
        }
    }

    @GetMapping("/{bookingCode}")
    public ResponseEntity<LastBookingDTO> getBookings(
            @PathVariable String bookingCode
    ) {

        LastBookingDTO lastBookingDTO = bookingService.getLastBooking(bookingCode);

        return ResponseEntity.ok(lastBookingDTO);
    }
}

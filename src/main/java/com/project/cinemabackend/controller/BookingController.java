package com.project.cinemabackend.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cinemabackend.dto.LastBookingDTO;
import com.project.cinemabackend.dto.payu.BookingRequest;
import com.project.cinemabackend.dto.payu.BookingResponse;
import com.project.cinemabackend.dto.payu.PayUNotification;
import com.project.cinemabackend.model.BookingStatus;
import com.project.cinemabackend.service.BookingService;
import com.project.cinemabackend.service.PayUService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;
    private final PayUService payUService;

    public BookingController(BookingService bookingService, PayUService payUService) {
        this.bookingService = bookingService;
        this.payUService = payUService;
    }

    @PostMapping("/public/booking")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        BookingResponse response = bookingService.createBookingWithPayment(httpRequest, request, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/public/booking/payu/notify", consumes = "application/json")
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

    @GetMapping("/public/booking/{bookingCode}")

    public ResponseEntity<LastBookingDTO> getBookings(
            @PathVariable String bookingCode
    ) {

        LastBookingDTO lastBookingDTO = bookingService.getLastBooking(bookingCode);

        return ResponseEntity.ok(lastBookingDTO);
    }

    @GetMapping ("/user/booking/history")
    ResponseEntity<Page<LastBookingDTO>> getLastBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(bookingService.getHistoryBookingList(
                authentication,
                status,
                from,
                to,
                minAmount,
                maxAmount,
                pageable)
        );
    }
}

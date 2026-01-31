package com.project.cinemabackend.service;

import com.project.cinemabackend.config.PayUProperties;
import com.project.cinemabackend.dto.LastBookingDTO;
import com.project.cinemabackend.dto.payu.*;
import com.project.cinemabackend.exception.BookingNotFoundException;
import com.project.cinemabackend.exception.PaymentException;
import com.project.cinemabackend.exception.SeatNotAvailableException;
import com.project.cinemabackend.mapper.BookingMapper;
import com.project.cinemabackend.mapper.PayUMapper;
import com.project.cinemabackend.model.*;
import com.project.cinemabackend.repository.*;
import com.project.cinemabackend.security.UserPrincipal;
import com.project.cinemabackend.util.ClientIP;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final PayUService payUService;
    private final PayUProperties payUProperties;
    private final PayUMapper payUMapper;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    public BookingService(PayUService payUService,
                          PayUProperties payUProperties,
                          PayUMapper payUMapper,
                          BookingRepository bookingRepository,
                          BookingSeatRepository bookingSeatRepository,
                          SeatRepository seatRepository,
                          ShowtimeRepository showtimeRepository,
                          PaymentRepository paymentRepository,
                          UserRepository userRepository,
                          BookingMapper bookingMapper) {
        this.payUService = payUService;
        this.payUProperties = payUProperties;
        this.payUMapper = payUMapper;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatRepository = seatRepository;
        this.showtimeRepository = showtimeRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public BookingResponse createBookingWithPayment(HttpServletRequest httpRequest, BookingRequest request, Authentication authentication) {
        boolean isGuest = true;

        ClientIP clientIP = new ClientIP();
        String clientIp = clientIP.getClientIp(httpRequest);
        request.setCustomerIp(clientIp);

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new BookingNotFoundException("Showtime not found"));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new SeatNotAvailableException("Some seats not found");
        }


        List<BookingSeat> bkSeats = bookingSeatRepository.findBookingSeatsBySeat_IdInAndBooking_Showtime_Id(request.getSeatIds(), request.getShowtimeId());
        for (Seat seat : seats) {
            if (!seat.getIsAvailable()) {
                throw new SeatNotAvailableException("Seat " + seat.getId() + " is not available");
            }
            if (bkSeats.stream().anyMatch(bs ->
                    bs.getSeat().getId().equals(seat.getId()) &&
                            bs.getStatus() == TicketStatus.VALID)) {
                throw new SeatNotAvailableException("Seat " + seat.getId() + " has already been booked");
            }
        }

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setShowtime(showtime);
        booking.setGuestEmail(request.getGuestEmail());
        booking.setGuestPhone(request.getGuestPhone());
        booking.setStatus(BookingStatus.CREATED);
        booking.setBookingCode(generateBookingCode());
        booking.setExpiresAt(OffsetDateTime.now().plusSeconds(135));
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setGuestPhone(request.getGuestPhone());

        if (authentication != null && authentication.isAuthenticated()) {
            isGuest = false;
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            UUID userId = principal.getUserId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));

            booking.setUser(user);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingSeat> bookingSeats = new ArrayList<>();

        for (Seat seat : seats) {
            BigDecimal price = calculateSeatPrice(seat, showtime);
            totalAmount = totalAmount.add(price);

            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setId(UUID.randomUUID());
            bookingSeat.setBooking(booking);
            bookingSeat.setSeat(seat);
            bookingSeat.setPrice(price);
            bookingSeat.setTicketCode(UUID.randomUUID());
            bookingSeat.setCreatedAt(OffsetDateTime.now());
            bookingSeat.setStatus(TicketStatus.PENDING);

            bookingSeats.add(bookingSeat);
        }

        booking.setTotalAmount(totalAmount);
        booking.setSeats(bookingSeats);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBooking(booking);
        payment.setAmount(totalAmount);
        payment.setPaymentMethod("");
        payment.setPaymentStatus("pending");
        payment.setProvider("payu");
        payment.setCreatedAt(OffsetDateTime.now());

        payment.setBooking(booking);
        booking.setPayment(payment);

        bookingRepository.save(booking);


        PayUOrderRequest payuRequest = buildPayUOrderRequest(booking, request, isGuest);
        PayUOrderResponse payuResponse = payUService.createOrder(payuRequest);

        if (payuResponse != null && "SUCCESS".equals(payuResponse.getStatus().getStatusCode())) {
            payment.setExpiresAt(OffsetDateTime.now().plusMinutes(2));
            payment.setTransactionId(payuResponse.getOrderId());
            payment.setPaymentUrl(payuResponse.getRedirectUri());
            paymentRepository.save(payment);

            return payUMapper.toBookingResponse(
                    booking.getId(),
                    booking.getBookingCode(),
                    totalAmount,
                    payuResponse.getRedirectUri(),
                    payuResponse.getOrderId()
            );
        } else {
            throw new PaymentException("Failed to create PayU order");
        }
    }

    private PayUOrderRequest buildPayUOrderRequest(Booking booking, BookingRequest request, boolean isGuest) {
        PayUOrderRequest payuRequest = new PayUOrderRequest();
        payuRequest.setContinueUrl(payUProperties.getContinueUrl());
        payuRequest.setNotifyUrl(payUProperties.getNotifyUrl());
        payuRequest.setCustomerIp(request.getCustomerIp());
        payuRequest.setMerchantPosId(payUProperties.getPosId());
        payuRequest.setDescription("Rezerwacja - " + booking.getBookingCode());
        payuRequest.setCurrencyCode("PLN");

        String amountInGrosze = booking.getTotalAmount()
                .multiply(new BigDecimal("100"))
                .toBigInteger()
                .toString();
        payuRequest.setTotalAmount(amountInGrosze);
        payuRequest.setExtOrderId(booking.getId().toString());

        PayUOrderRequest.Buyer buyer = new PayUOrderRequest.Buyer();
        if (isGuest) {
            buyer.setEmail(request.getGuestEmail());
            buyer.setPhone(request.getGuestPhone());
            buyer.setFirstName(request.getGuestFirstName());
            buyer.setLastName(request.getGuestLastName());
        } else {
            buyer.setEmail(booking.getUser().getEmail());
            buyer.setPhone(booking.getUser().getPhone());
            buyer.setFirstName(booking.getUser().getFirstName());
            buyer.setLastName(booking.getUser().getLastName());
        }

        buyer.setLanguage("pl");
        payuRequest.setBuyer(buyer);

        List<PayUOrderRequest.Product> products = new ArrayList<>();
        for (BookingSeat bookingSeat : booking.getSeats()) {
            PayUOrderRequest.Product product = new PayUOrderRequest.Product();
            product.setName("Miejsce " + bookingSeat.getSeat().getRowNumber() +
                    "-" + bookingSeat.getSeat().getSeatNumber());
            product.setUnitPrice(bookingSeat.getPrice()
                    .multiply(new BigDecimal("100"))
                    .toBigInteger()
                    .toString());
            product.setQuantity("1");
            products.add(product);
        }
        payuRequest.setProducts(products);

        return payuRequest;
    }

    private BigDecimal calculateSeatPrice(Seat seat, Showtime showtime) {
        String seatType = seat.getSeatType();

        if ("vip".equalsIgnoreCase(seatType) && showtime.getVipPrice() != null) {
            return showtime.getVipPrice();
        } else if ("premium".equalsIgnoreCase(seatType) && showtime.getPremiumPrice() != null) {
            return showtime.getPremiumPrice();
        } else {
            return showtime.getBasePrice();
        }
    }

    private String generateBookingCode() {
        return "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }



        @Transactional
    public void handlePaymentNotification(PayUNotification notification) {
        String extOrderId = notification.getOrder().getExtOrderId();
        UUID bookingId = UUID.fromString(extOrderId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        Payment payment = booking.getPayment();

        String status = notification.getOrder().getStatus();

        if ("COMPLETED".equals(status)) {
            booking.setStatus(BookingStatus.PAID);
            payment.setPaymentStatus("completed");
            payment.setPaidAt(OffsetDateTime.now());

            for (BookingSeat bookingSeat : booking.getSeats()) {
                Seat seat = bookingSeat.getSeat();
                bookingSeat.setStatus(TicketStatus.VALID);
                seatRepository.save(seat);
            }
        } else if ("CANCELED".equals(status)) {
            booking.setStatus(BookingStatus.CANCELLED);
            payment.setPaymentStatus("cancelled");
            payment.setPaymentUrl(null);

            for (BookingSeat bookingSeat : booking.getSeats()) {
                bookingSeat.setStatus(TicketStatus.CANCELLED);
            }
        }

        booking.setUpdatedAt(OffsetDateTime.now());
        bookingRepository.save(booking);
        paymentRepository.save(payment);
    }

    public LastBookingDTO getLastBooking(String code) {
        Booking booking = bookingRepository.findByBookingCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if(!booking.getStatus().equals(BookingStatus.PAID)) {
            return null;
        }

        LastBookingDTO dto = bookingMapper.toLastBookingDto(booking);
        booking.setBookingCode(null);
        bookingRepository.save(booking);

        return dto;
    }

    public Page<LastBookingDTO> getHistoryBookingList(
            Authentication authentication,
            BookingStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated"
            );
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UUID userId = principal.getUserId();


        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return bookingRepository.findUserBookings(
                userId,
                status,
                from,
                to,
                minAmount,
                maxAmount,
                sorted).map(bookingMapper::toLastBookingDto);
    }

}

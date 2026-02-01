package com.project.cinemabackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cinemabackend.dto.payu.BookingRequest;
import com.project.cinemabackend.dto.payu.BookingResponse;
import com.project.cinemabackend.dto.payu.PayUNotification;
import com.project.cinemabackend.dto.payu.PayUOrderResponse;
import com.project.cinemabackend.model.*;
import com.project.cinemabackend.repository.*;
import com.project.cinemabackend.security.UserPrincipal;
import com.project.cinemabackend.service.PayUService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public PayUService payUService() {
            return Mockito.mock(PayUService.class);
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("cinema_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PayUService payUService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private UserRepository userRepository;

    private Cinema cinema;
    private Hall hall;
    private Movie movie;
    private Showtime showtime;
    private Seat seat1;
    private Seat seat2;
    private User user;

    @BeforeEach
    void setUp() {
        Mockito.reset(payUService);
        bookingSeatRepository.deleteAll();
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        showtimeRepository.deleteAll();
        hallRepository.deleteAll();
        movieRepository.deleteAll();
        cinemaRepository.deleteAll();
        userRepository.deleteAll();

        cinema = createCinema();
        hall = createHall(cinema);
        Movie movie = createMovie(
                "Skazani na Shawshank",
                "The Shawshank Redemption",
                "Prison drama about hope and friendship",
                142,
                LocalDate.of(1994, 9, 23),
                "15",
                true,
                false,
                false
        );
        showtime = createShowtime(movie, hall);
        seat1 = createSeat(hall, 1, 1, "standard");
        seat2 = createSeat(hall, 1, 2, "vip");
        user = createUser();
    }

    @Test
    @Order(1)
    @DisplayName("Powinien utworzyć rezerwację dla gościa(niezalogowany) z prawidłową odpowiedzią PayU")
    @Transactional
    void shouldCreateBookingAsGuest() throws Exception {
        PayUOrderResponse mockPayUResponse = assignPayUOrderResponse("SUCCESS", "https://payu.com/pay/123", "PAYU-ORDER-123");
        when(payUService.createOrder(any())).thenReturn(mockPayUResponse);

        BookingRequest request = assignBookingRequest(
                List.of(seat1.getId(), seat2.getId()),
                "guest@example.com",
                "987654321",
                "Guest",
                "User"
        );

        mockMvc.perform(post("/api/public/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingCode").exists())
                .andExpect(jsonPath("$.totalAmount").value(70.00))
                .andExpect(jsonPath("$.redirectUrl").value("https://payu.com/pay/123"))
                .andExpect(jsonPath("$.payuOrderId").value("PAYU-ORDER-123"));

        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        Booking booking = bookings.get(0);
        assertThat(booking.getGuestEmail()).isEqualTo("guest@example.com");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CREATED);
        assertThat(booking.getSeats()).hasSize(2);
        assertThat(booking.getPayment()).isNotNull();
    }

    @Test
    @Order(2)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Powinien utworzyć rezerwację dla zalogowanego użytkownika")
    void shouldCreateBookingAsAuthenticatedUser() throws Exception {
        PayUOrderResponse mockPayUResponse = assignPayUOrderResponse("SUCCESS", "https://payu.com/pay/456", "PAYU-ORDER-456");
        when(payUService.createOrder(any())).thenReturn(mockPayUResponse);

        Authentication auth = createAuthentication(user);

        BookingRequest request = assignBookingRequest(
                List.of(seat1.getId()),
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/public/booking")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingCode").exists())
                .andExpect(jsonPath("$.totalAmount").value(25.00));

        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
    }

    @Test
    @Order(3)
    @DisplayName("Powinien obsłużyć powiadomienie PayU dla zakończonej płatności i zaktualizować status rezerwacji")
    @Transactional
    void shouldHandlePayUNotificationForCompletedPayment() throws Exception {
        Booking booking = createTestBooking();

        PayUNotification notification = assignPayUNotification(
                booking.getId().toString(),
                "COMPLETED",
                "PAYU-ORDER-789"
        );

        String notificationJson = objectMapper.writeValueAsString(notification);

        when(payUService.validateAndParsePayUNotification(anyString(), anyString()))
                .thenReturn(notification);

        mockMvc.perform(post("/api/public/booking/payu/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("OpenPayu-Signature", "signature=test;algorithm=MD5")
                        .content(notificationJson))
                .andExpect(status().isOk());

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.PAID);
        assertThat(updatedBooking.getPayment().getPaymentStatus()).isEqualTo("completed");
        assertThat(updatedBooking.getPayment().getPaidAt()).isNotNull();

        for (BookingSeat bookingSeat : updatedBooking.getSeats()) {
            assertThat(bookingSeat.getStatus()).isEqualTo(TicketStatus.VALID);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Powinien obsłużyć powiadomienie PayU dla anulowanej płatności i zaktualizować status rezerwacji")
    @Transactional
    void shouldHandlePayUNotificationForCancelledPayment() throws Exception {
        Booking booking = createTestBooking();

        PayUNotification notification = assignPayUNotification(
                booking.getId().toString(),
                "CANCELED",
                "PAYU-ORDER-CANCELLED"
        );

        String notificationJson = objectMapper.writeValueAsString(notification);

        when(payUService.validateAndParsePayUNotification(anyString(), anyString()))
                .thenReturn(notification);

        mockMvc.perform(post("/api/public/booking/payu/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("OpenPayu-Signature", "signature=test;algorithm=MD5")
                        .content(notificationJson))
                .andExpect(status().isOk());

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(updatedBooking.getPayment().getPaymentStatus()).isEqualTo("cancelled");

        for (BookingSeat bookingSeat : updatedBooking.getSeats()) {
            assertThat(bookingSeat.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        }
    }

    @Test
    @Order(5)
    @DisplayName("Powinien pobrać rezerwację po kodzie dla opłaconej rezerwacji i wyczyścić kod rezerwacji")
    void shouldGetBookingByCode() throws Exception {
        Booking booking = createTestBooking();
        booking.setStatus(BookingStatus.PAID);
        booking = bookingRepository.save(booking);

        String bookingCode = booking.getBookingCode();

        mockMvc.perform(get("/api/public/booking/{bookingCode}", bookingCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.status").value("PAID"));

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updatedBooking.getBookingCode()).isNull();
    }

    @Test
    @Order(6)
    @DisplayName("Powinien zwrócić null dla nieopłaconej rezerwacji przy zapytaniu po kodzie")
    void shouldReturnNullForNonPaidBooking() throws Exception {
        Booking booking = createTestBooking();
        String bookingCode = booking.getBookingCode();

        mockMvc.perform(get("/api/public/booking/{bookingCode}", bookingCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "test@example.com")
    @DisplayName("Powinien pobrać historię rezerwacji dla zalogowanego użytkownika")
    void shouldGetBookingHistory() throws Exception {
        Authentication auth = createAuthentication(user);

        Booking booking1 = createTestBooking();
        booking1.setUser(user);
        booking1.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking1);

        Booking booking2 = createTestBooking();
        booking2.setUser(user);
        booking2.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking2);

        mockMvc.perform(get("/api/user/booking/history")
                        .with(authentication(auth))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @Order(8)
    @DisplayName("Powinien rzucić wyjątek gdy miejsce nie jest dostępne")
    void shouldThrowExceptionWhenSeatNotAvailable() throws Exception {
        seat1.setIsAvailable(false);
        seatRepository.save(seat1);

        BookingRequest request = assignBookingRequest(
                List.of(seat1.getId()),
                "guest@example.com",
                "987654321",
                "Guest",
                "User"
        );

        mockMvc.perform(post("/api/public/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(9)
    @DisplayName("Powinien rzucić wyjątek gdy miejsce jest już zarezerwowane z ważnym biletem")
    void shouldThrowExceptionWhenSeatAlreadyBooked() throws Exception {
        Booking existingBooking = createTestBooking();
        existingBooking.setStatus(BookingStatus.PAID);
        for (BookingSeat bs : existingBooking.getSeats()) {
            bs.setStatus(TicketStatus.VALID);
        }
        bookingRepository.save(existingBooking);

        BookingRequest request = assignBookingRequest(
                List.of(seat1.getId()),
                "guest2@example.com",
                "111222333",
                "Guest2",
                "User2"
        );

        mockMvc.perform(post("/api/public/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }


    private Cinema createCinema() {
        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");
        cinema.setCity("Test City");
        cinema.setAddress("Test Address 123");
        cinema.setIsActive(true);
        cinema.setCreatedAt(OffsetDateTime.now());
        return cinemaRepository.save(cinema);
    }

    private Hall createHall(Cinema cinema) {
        Hall hall = new Hall();
        hall.setCinema(cinema);
        hall.setName("Hall 1");
        hall.setTotalSeats(100);
        hall.setScreenType("standard");
        hall.setHas3d(false);
        hall.setIsActive(true);
        hall.setCreatedAt(OffsetDateTime.now());
        return hallRepository.save(hall);
    }

    private Movie createMovie(String title, String originalTitle, String description,
                              Integer durationMinutes, LocalDate releaseDate, String ageRating,
                              Boolean isActive, Boolean isRecommended, Boolean isUpcoming) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setOriginalTitle(originalTitle);
        movie.setDescription(description);
        movie.setDurationMinutes(durationMinutes);
        movie.setReleaseDate(releaseDate);
        movie.setAgeRating(ageRating);
        movie.setLanguage("Polish");
        movie.setCountry("USA");
        movie.setIsActive(isActive);
        movie.setIsRecommended(isRecommended);
        movie.setIsUpcoming(isUpcoming);
        movie.setAverageRating(BigDecimal.ZERO);
        movie.setRatingCount(0);
        movie.setHasSubtitles(false);
        movie.setHasLector(false);
        movie.setHasDubbing(false);
        movie.setIsOriginalLanguage(true);
        movie.setCreatedAt(OffsetDateTime.now());
        movie.setUpdatedAt(OffsetDateTime.now());

        return movieRepository.save(movie);
    }

    private Showtime createShowtime(Movie movie, Hall hall) {
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setHall(hall);

        showtime.setStartTime(OffsetDateTime.now().plusDays(1));
        showtime.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(2));

        showtime.setBasePrice(new BigDecimal("25.00"));
        showtime.setPremiumPrice(new BigDecimal("35.00"));
        showtime.setVipPrice(new BigDecimal("45.00"));

        showtime.setIs3d(false);
        showtime.setLanguage("Polish");
        showtime.setHasSubtitles(false);
        showtime.setAudioTrack(AudioTrackType.ORIGINAL);
        showtime.setSubtitles(null);

        showtime.setCreatedAt(OffsetDateTime.now());
        showtime.setIsActive(true);

        return showtimeRepository.save(showtime);
    }


    private Seat createSeat(Hall hall, int rowNumber, int seatNumber, String seatType) {
        Seat seat = new Seat();
        seat.setHall(hall);
        seat.setRowNumber(rowNumber);
        seat.setSeatNumber(seatNumber);
        seat.setSeatType(seatType);
        seat.setIsAvailable(true);
        seat.setCreatedAt(OffsetDateTime.now());
        return seatRepository.save(seat);
    }

    private User createUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhone("123456789");
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    private BookingRequest assignBookingRequest(List<UUID> seatIds, String email, String phone, String firstName, String lastName) {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(showtime.getId());
        request.setSeatIds(seatIds);
        request.setGuestEmail(email);
        request.setGuestPhone(phone);
        request.setGuestFirstName(firstName);
        request.setGuestLastName(lastName);
        return request;
    }

    private PayUOrderResponse assignPayUOrderResponse(String statusCode, String redirectUri, String orderId) {
        PayUOrderResponse mockPayUResponse = new PayUOrderResponse();
        PayUOrderResponse.Status status = new PayUOrderResponse.Status();
        status.setStatusCode(statusCode);
        mockPayUResponse.setStatus(status);
        mockPayUResponse.setRedirectUri(redirectUri);
        mockPayUResponse.setOrderId(orderId);
        return mockPayUResponse;
    }

    private PayUNotification assignPayUNotification(String extOrderId, String status, String orderId) {
        PayUNotification notification = new PayUNotification();
        PayUNotification.Order order = new PayUNotification.Order();
        order.setExtOrderId(extOrderId);
        order.setStatus(status);
        order.setOrderId(orderId);
        notification.setOrder(order);
        return notification;
    }

    private Booking createTestBooking() {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setShowtime(showtime);
        booking.setGuestEmail("test@example.com");
        booking.setGuestPhone("123456789");
        booking.setStatus(BookingStatus.CREATED);
        booking.setBookingCode("BK-" + UUID.randomUUID().toString().substring(0, 16));
        booking.setTotalAmount(new BigDecimal("25.00"));
        booking.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());

        BookingSeat bookingSeat = createBookingSeat(booking, seat1, new BigDecimal("25.00"));
        booking.setSeats(List.of(bookingSeat));

        Payment payment = createPayment(booking, new BigDecimal("25.00"));
        booking.setPayment(payment);

        return bookingRepository.save(booking);
    }

    private BookingSeat createBookingSeat(Booking booking, Seat seat, BigDecimal price) {
        BookingSeat bookingSeat = new BookingSeat();
        bookingSeat.setId(UUID.randomUUID());
        bookingSeat.setBooking(booking);
        bookingSeat.setSeat(seat);
        bookingSeat.setPrice(price);
        bookingSeat.setTicketCode(UUID.randomUUID());
        bookingSeat.setStatus(TicketStatus.PENDING);
        bookingSeat.setCreatedAt(OffsetDateTime.now());
        return bookingSeat;
    }

    private Payment createPayment(Booking booking, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentMethod("");
        payment.setPaymentStatus("pending");
        payment.setProvider("payu");
        payment.setTransactionId("PAYU-" + UUID.randomUUID());
        payment.setCreatedAt(OffsetDateTime.now());
        return payment;
    }

    private Authentication createAuthentication(User user) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }
}
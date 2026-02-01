package com.project.cinemabackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cinemabackend.model.*;
import com.project.cinemabackend.model.Tag;
import com.project.cinemabackend.repository.*;
import com.project.cinemabackend.security.UserPrincipal;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.*;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AiRecommendationControllerIntegrationTest {

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

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public OpenAiService openAiService() {
            return mock(OpenAiService.class);
        }
    }

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private UserRatingRepository userRatingRepository;

    @Autowired
    private MovieTagRepository movieTagRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User testUser;
    private List<Movie> testMovies;
    private List<Tag> testTags;
    private List<Genre> testGenres;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRatingRepository.deleteAll();
        movieTagRepository.deleteAll();
        movieGenreRepository.deleteAll();
        movieRepository.deleteAll();
        tagRepository.deleteAll();
        genreRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = createRole("USER", "Standard user role");
        userRole = roleRepository.save(userRole);

        testUser = createUser(
                "test@example.com",
                "hashedPassword123",
                "Jan",
                "Kowalski",
                "+48123456789"
        );
        testUser = userRepository.save(testUser);

        assignRoleToUser(testUser, userRole);

        testGenres = new ArrayList<>();
        Genre action = createGenre("Action", "Action movies");
        Genre drama = createGenre("Drama", "Dramatic films");
        Genre comedy = createGenre("Comedy", "Comedy films");
        Genre sciFi = createGenre("Sci-Fi", "Science Fiction");
        testGenres.addAll(List.of(action, drama, comedy, sciFi));
        testGenres =(List<Genre>) genreRepository.saveAll(testGenres);

        testTags = new ArrayList<>();
        Tag futureTag = createTag("Przyszłość", "Films about future");
        Tag familyTag = createTag("Rodzinny", "Family friendly");
        Tag thrillerTag = createTag("Thriller", "Thrilling content");
        Tag visualTag = createTag("Efekty wizualne", "Great visual effects");
        testTags.addAll(List.of(futureTag, familyTag, thrillerTag, visualTag));
        testTags =(List<Tag>) tagRepository.saveAll(testTags);

        testMovies = new ArrayList<>();

        Movie movie1 = createMovie(
                "Matrix",
                "The Matrix",
                "A cyberpunk action film about reality",
                136,
                LocalDate.of(1999, 3, 31),
                "15",
                true,
                false,
                false
        );

        Movie movie2 = createMovie(
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

        Movie movie3 = createMovie(
                "Kac Vegas",
                "The Hangover",
                "Comedy about bachelor party gone wrong",
                100,
                LocalDate.of(2009, 6, 5),
                "15",
                true,
                false,
                false
        );

        Movie movie4 = createMovie(
                "Interstellar",
                "Interstellar",
                "Space exploration epic",
                169,
                LocalDate.of(2014, 11, 7),
                "12",
                true,
                false,
                false
        );

        Movie movie5 = createMovie(
                "Blade Runner 2049",
                "Blade Runner 2049",
                "Sci-fi detective story",
                164,
                LocalDate.of(2017, 10, 6),
                "15",
                false,
                false,
                false
        );

        testMovies.addAll(List.of(movie1, movie2, movie3, movie4, movie5));
        testMovies = movieRepository.saveAll(testMovies);

        assignGenreToMovie(movie1, sciFi);
        assignGenreToMovie(movie1, action);
        assignGenreToMovie(movie2, drama);
        assignGenreToMovie(movie3, comedy);
        assignGenreToMovie(movie4, sciFi);
        assignGenreToMovie(movie4, drama);

        assignTagToMovie(movie1, futureTag);
        assignTagToMovie(movie1, visualTag);
        assignTagToMovie(movie2, thrillerTag);
        assignTagToMovie(movie4, futureTag);
        assignTagToMovie(movie4, visualTag);

        createUserRating(testUser, movie1, 5, null);
        createUserRating(testUser, movie2, 4, null);
        createUserRating(testUser, movie4, 5, null);
    }

    @Test
    @Order(1)
    @DisplayName("Powinien zwrócić rekomendacje AI dla zalogowanego użytkownika")
    void shouldReturnAiRecommendationsForAuthenticatedUser() throws Exception {
        String mockAiResponse = """
            [
              {
                "movieId": "%s",
                "title": "Matrix",
                "match_percent": 95,
                "predicted_rating": 4.8
              },
              {
                "movieId": "%s",
                "title": "Interstellar",
                "match_percent": 88,
                "predicted_rating": 4.5
              },
              {
                "movieId": "%s",
                "title": "Skazani na Shawshank",
                "match_percent": 75,
                "predicted_rating": 4.2
              },
              {
                "movieId": "%s",
                "title": "Kac Vegas",
                "match_percent": 45,
                "predicted_rating": 3.0
              }
            ]
            """.formatted(
                testMovies.get(0).getId(),
                testMovies.get(3).getId(),
                testMovies.get(1).getId(),
                testMovies.get(2).getId()
        );

        mockOpenAiService(mockAiResponse);

        Authentication auth = createAuthentication(testUser);

        mockMvc.perform(get("/api/user/ai/recommendation")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].title", is("Matrix")))
                .andExpect(jsonPath("$[0].match_percent", is(95)))
                .andExpect(jsonPath("$[0].predicted_rating", is(4.8)))
                .andExpect(jsonPath("$[1].title", is("Interstellar")))
                .andExpect(jsonPath("$[1].match_percent", is(88)))
                .andExpect(jsonPath("$[3].title", is("Kac Vegas")))
                .andExpect(jsonPath("$[3].match_percent", is(45)));
    }

    @Test
    @Order(2)
    @DisplayName("Powinien zwrócić 401 dla niezalogowanego użytkownika")
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/user/ai/recommendation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("Powinien zwrócić 500 gdy AI zwróci niepoprawny JSON")
    void shouldReturn500WhenAiReturnsInvalidJson() throws Exception {
        String invalidJsonResponse = "{ invalid json }";
        mockOpenAiService(invalidJsonResponse);

        Authentication auth = createAuthentication(testUser);

        mockMvc.perform(get("/api/user/ai/recommendation")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Coś poszło nie tak")));
    }

    @Test
    @Order(4)
    @DisplayName("Powinien zwrócić 500 gdy AI zwróci zduplikowane movieId")
    void shouldReturn500WhenAiReturnsDuplicateMovieIds() throws Exception {
        String duplicateResponse = """
            [
              {
                "movieId": "%s",
                "title": "Matrix",
                "match_percent": 95,
                "predicted_rating": 4.8
              },
              {
                "movieId": "%s",
                "title": "Matrix Again",
                "match_percent": 90,
                "predicted_rating": 4.7
              }
            ]
            """.formatted(testMovies.getFirst().getId(), testMovies.getFirst().getId());

        mockOpenAiService(duplicateResponse);

        Authentication auth = createAuthentication(testUser);

        mockMvc.perform(get("/api/user/ai/recommendation")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Coś poszło nie tak")));
    }

    @Test
    @Order(5)
    @DisplayName("Powinien zwrócić 500 gdy AI zwróci wartości poza zakresem")
    void shouldReturn500WhenAiReturnsOutOfRangeValues() throws Exception {
        String outOfRangeResponse = """
            [
              {
                "movieId": "%s",
                "title": "Matrix",
                "match_percent": 150,
                "predicted_rating": 6.5
              }
            ]
            """.formatted(testMovies.getFirst().getId());

        mockOpenAiService(outOfRangeResponse);

        Authentication auth = createAuthentication(testUser);

        mockMvc.perform(get("/api/user/ai/recommendation")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Coś poszło nie tak")));
    }

    @Test
    @Order(6)
    @DisplayName("Powinien zwrócić tylko aktywne filmy w rekomendacjach")
    void shouldReturnOnlyActiveMoviesInRecommendations() throws Exception {
        String mockAiResponse = """
            [
              {
                "movieId": "%s",
                "title": "Matrix",
                "match_percent": 95,
                "predicted_rating": 4.8
              },
              {
                "movieId": "%s",
                "title": "Interstellar",
                "match_percent": 88,
                "predicted_rating": 4.5
              }
            ]
            """.formatted(
                testMovies.get(0).getId(), //matrix->active
                testMovies.get(3).getId()  //interstellar->active
        );

        mockOpenAiService(mockAiResponse);

        Authentication auth = createAuthentication(testUser);

        mockMvc.perform(get("/api/user/ai/recommendation")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Matrix")))
                .andExpect(jsonPath("$[1].title", is("Interstellar")));
    }

    private void mockOpenAiService(String jsonResponse) {
        ChatMessage responseMessage = new ChatMessage("assistant", jsonResponse);
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setMessage(responseMessage);

        ChatCompletionResult result = new ChatCompletionResult();
        result.setChoices(List.of(choice));

        when(openAiService.createChatCompletion(any()))
                .thenReturn(result);
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


    private User createUser(String email, String passwordHash,
                            String firstName, String lastName, String phone) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }

    private Role createRole(String roleName, String description) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setCreatedAt(OffsetDateTime.now());
        return role;
    }

    private void assignRoleToUser(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedAt(OffsetDateTime.now());
        userRoleRepository.save(userRole);
    }

    private Genre createGenre(String name, String description) {
        Genre genre = new Genre();
        genre.setName(name);
        genre.setDescription(description);
        genre.setCreatedAt(OffsetDateTime.now());
        return genre;
    }

    private Tag createTag(String name, String description) {
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(description);
        tag.setCreatedAt(OffsetDateTime.now());
        return tag;
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
        return movie;
    }

    private void assignGenreToMovie(Movie movie, Genre genre) {
        MovieGenre movieGenre = new MovieGenre();
        movieGenre.setMovie(movie);
        movieGenre.setGenre(genre);
        movieGenreRepository.save(movieGenre);
    }

    private void assignTagToMovie(Movie movie, Tag tag) {
        MovieTag movieTag = new MovieTag();
        movieTag.setMovie(movie);
        movieTag.setTag(tag);
        movieTag.setCreatedAt(OffsetDateTime.now());
        movieTagRepository.save(movieTag);
    }

    private UserRating createUserRating(User user, Movie movie, Integer rating, String reviewText) {
        UserRating userRating = new UserRating();
        userRating.setUser(user);
        userRating.setMovie(movie);
        userRating.setRating(rating);
        userRating.setReviewText(reviewText);
        userRating.setCreatedAt(OffsetDateTime.now());
        userRating.setUpdatedAt(OffsetDateTime.now());
        return userRatingRepository.save(userRating);
    }

    @AfterAll
    static void tearDown() {
        postgres.stop();
    }
}
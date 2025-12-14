package com.project.cinemabackend.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cinemabackend.dto.ai.AiResponse;
import com.project.cinemabackend.dto.ai.MovieAiDTO;
import com.project.cinemabackend.dto.ai.MovieAiRatingDTO;
import com.project.cinemabackend.dto.ai.TagMinimalDTO;
import com.project.cinemabackend.mapper.MovieMapper;
import com.project.cinemabackend.mapper.MovieTagMapper;
import com.project.cinemabackend.mapper.RatingMapper;
import com.project.cinemabackend.model.Tag;
import com.project.cinemabackend.repository.MovieRepository;
import com.project.cinemabackend.repository.RatingRepository;
import com.project.cinemabackend.repository.TagRepository;
import com.project.cinemabackend.util.AiResponseValidator;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AiRecommendationService {
    TagRepository tagRepository;
    MovieRepository movieRepository;
    MovieTagMapper movieTagMapper;
    MovieMapper  movieMapper;
    RatingRepository ratingRepository;
    RatingMapper ratingMapper;
    private final OpenAiService openAiService;
    private static final Logger log =
            LoggerFactory.getLogger(AiRecommendationService.class);

    AiRecommendationService(TagRepository tagRepository,  MovieTagMapper movieTagMapper,  MovieRepository movieRepository,  MovieMapper movieMapper, RatingRepository ratingRepository, RatingMapper ratingMapper, OpenAiService openAiService) {
        this.tagRepository = tagRepository;
        this.movieTagMapper =  movieTagMapper;
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.ratingRepository = ratingRepository;
        this.ratingMapper = ratingMapper;
        this.openAiService = openAiService;
    }

    public List<AiResponse> findRecommendationByAI(UUID userId) {

        log.info("Starting AI recommendation for userId={}", userId);

        try {
            List<TagMinimalDTO> tags =
                    movieTagMapper.toTagMinimalDTOList((List<Tag>) tagRepository.findAll());

            List<MovieAiDTO> moviesAiDTOs =
                    movieMapper.toMovieAiDtoList(movieRepository.findByIsActiveTrue());

            Pageable limit15 = PageRequest.of(0, 15);

            List<MovieAiRatingDTO> ratings =
                    ratingMapper.toMovieAiRatingDtoList(
                            ratingRepository.findByUserIdOrderByCreatedAtDesc(userId, limit15)
                    );

            ObjectMapper objectMapper = new ObjectMapper();

            String tagsJson = objectMapper.writeValueAsString(tags);
            String ratedMoviesJson = objectMapper.writeValueAsString(ratings);
            String repertoireJson = objectMapper.writeValueAsString(moviesAiDTOs);

            StringBuilder prompt = new StringBuilder();

            prompt.append("""
            Jesteś silnikiem rekomendacji filmowych dla kina.
            Twoim zadaniem jest przewidywanie dopasowania filmów do użytkownika
            na podstawie jego ostatnich ocen.
        
            Analizuj:
            - zgodność tagów i gatunków
            - rozkład ocen użytkownika
            - powtarzające się motywy
            - negatywne preferencje (niskie oceny)
        
            Zwracaj WYŁĄCZNIE poprawny JSON.
            Nie dodawaj żadnych komentarzy ani tekstu.
            Nie zmieniaj struktury odpowiedzi. **ODPOWIEDŹ MUSI ZAWIERAĆ TYLKO I WYŁĄCZNIE JSON.**
        
            DANE WEJŚCIOWE:
        
            TAGI:
            """);

            prompt.append(tagsJson);

            prompt.append("""
            
            HISTORIA OCEN UŻYTKOWNIKA (max 15):
            """);

            prompt.append(ratedMoviesJson);

            prompt.append("""
            
            AKTUALNY REPERTUAR KINA:
            """);

            prompt.append(repertoireJson);

            prompt.append("""
            
            ZADANIE:
            Dla **KAŻDEGO** filmu z repertuaru (i tylko dla filmów z repertuaru):
            - Oblicz procentowe dopasowanie do preferencji użytkownika (0–100).
            - Przewidź ocenę użytkownika (skala 0–5).
            - **UNIKALNOŚĆ:** Upewnij się, że **żaden film nie jest zduplikowany**; każdy `movieId` może pojawić się w wynikowej liście **TYLKO JEDEN RAZ**.
        
            SORTOWANIE:
            - sortuj malejąco po procentowym dopasowaniu
        
            FORMAT ODPOWIEDZI (ŚCIŚLE):
        
            [
              {
                "movieId": "uuid",
                "title": string,
                "match_percent": number,
                "predicted_rating": number
              }
            ]
            """);

        /*
        // Zakomentowana sekcja wysyłająca zapytanie do OpenAI:
        List<ChatMessage> prompts = List.of(
                new ChatMessage(
                        "system",
                        "Jesteś silnikiem rekomendacji filmowych. Zwracaj wyłącznie poprawny JSON."
                ),
                new ChatMessage("user", prompt.toString())
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(prompts)
                .model("gpt-4")
                .temperature(0.6)
                .maxTokens(1000)
                .build();

        long startTime = System.currentTimeMillis();

        ChatCompletionResult result =
                openAiService.createChatCompletion(request);

        long duration = System.currentTimeMillis() - startTime;

        log.info("AI response received for userId={}, duration={}ms",
                userId, duration);

        String jsonResponse =
                result.getChoices().get(0).getMessage().getContent();
        */

            // Stała odpowiedź JSON (mock)
            String jsonResponse = """
                    [
                       {
                         "movieId": "fb372204-1104-4bda-a8c9-004a1ac2ffc4",
                         "title": "Until Dawn",
                         "match_percent": 85,
                         "predicted_rating": 4.2
                       },
                       {
                         "movieId": "f8ad4db2-afd7-43c7-bb2f-fae632990e92",
                         "title": "Good Boy",
                         "match_percent": 80,
                         "predicted_rating": 3.9
                       },
                       {
                         "movieId": "f2c5fc19-db82-4b39-a776-cc9db48d6e5e",
                         "title": "Obecność 4: Ostatnie namaszczenie",
                         "match_percent": 78,
                         "predicted_rating": 3.8
                       },
                       {
                         "movieId": "39491384-c791-464d-9ab1-dd99cac2b529",
                         "title": "Harry Potter i Insygnia Śmierci: Część II",
                         "match_percent": 74,
                         "predicted_rating": 4.1
                       },
                       {
                         "movieId": "95793864-cbd3-4906-a880-c97391e5df2c",
                         "title": "Pan Wilk i spółka 2",
                         "match_percent": 70,
                         "predicted_rating": 3.5
                       },
                       {
                         "movieId": "f74c356b-6b63-4cd6-9ac4-2814341fa99c",
                         "title": "Shrek Trzeci",
                         "match_percent": 68,
                         "predicted_rating": 3.4
                       },
                       {
                         "movieId": "75101571-3bf1-464e-a73c-b4f7f612795d",
                         "title": "Król Lew",
                         "match_percent": 65,
                         "predicted_rating": 3.8
                       },
                       {
                         "movieId": "fdd89b6b-c3f8-4d57-a32f-8fade4566ab5",
                         "title": "Świąteczny skok",
                         "match_percent": 62,
                         "predicted_rating": 3.2
                       },
                       {
                         "movieId": "e6c9d562-8a47-4b41-9115-20a6345c0487",
                         "title": "Kac Vegas",
                         "match_percent": 60,
                         "predicted_rating": 3.1
                       },
                       {
                         "movieId": "71337d67-c114-4558-8373-401a6055841e",
                         "title": "TRON: Ares",
                         "match_percent": 58,
                         "predicted_rating": 3.0
                       },
                       {
                         "movieId": "2b262dab-bf2b-4492-b525-8ad494075156",
                         "title": "Predator: Strefa zagrożenia",
                         "match_percent": 56,
                         "predicted_rating": 2.9
                       },
                       {
                         "movieId": "1cbf8776-64bb-4392-a0a1-7e004af011e1",
                         "title": "Zwierzogród 2",
                         "match_percent": 54,
                         "predicted_rating": 2.8
                       },
                       {
                         "movieId": "9923d82e-ca8e-4615-96e9-f7f9311483ee",
                         "title": "Ojciec chrzestny",
                         "match_percent": 50,
                         "predicted_rating": 4.0
                       },
                       {
                         "movieId": "daa06fe0-f8f4-435c-bdea-395a9aebe817",
                         "title": "Skazani na Shawshank",
                         "match_percent": 50,
                         "predicted_rating": 4.0
                       }
                     ]
            """;

            System.out.println(jsonResponse);

            // zabezpieczenie przed markdown
            jsonResponse = jsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            List<AiResponse> responses =
                    objectMapper.readValue(
                            jsonResponse,
                            new TypeReference<List<AiResponse>>() {}
                    );

            AiResponseValidator.validate(responses);

            log.info("AI recommendations generated successfully for userId={}, count={}",
                    userId, responses.size());

            return responses;

        } catch (Exception e) {

            log.error("AI recommendation failed for userId={}", userId, e);

            throw new RuntimeException("AI recommendation failed", e);
        }
    }

}

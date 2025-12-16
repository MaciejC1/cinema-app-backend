package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.SurveyRequest;
import com.project.cinemabackend.model.Tag;
import com.project.cinemabackend.model.User;
import com.project.cinemabackend.model.UserEmbedding;
import com.project.cinemabackend.model.UserRating;
import com.project.cinemabackend.repository.GenreRepository;
import com.project.cinemabackend.repository.MovieEmbeddingRepository;
import com.project.cinemabackend.repository.TagRepository;
import com.project.cinemabackend.repository.UserEmbeddingRepository;
import com.project.cinemabackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserEmbeddingService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieEmbeddingRepository movieEmbeddingRepository;
    @Autowired
    private UserEmbeddingRepository userEmbeddingRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private TagRepository tagRepository;

    private static final double GENRE_WEIGHT = 2.0;
    private static final double TAG_WEIGHT = 1.0;
    private static final double FAVOURITE_GENRES_MULTIPLIER = 2.0;

    public void generateUserVectors() {
        List<User> users = (List<User>) userRepository.findAll();

        // Pobieramy wszystkie gatunki i tagi
        List<String> allGenres = StreamSupport.stream(genreRepository.findAll().spliterator(), false)
                .map(g -> g.getName())
                .collect(Collectors.toList());

        List<String> allTags = StreamSupport.stream(tagRepository.findAll().spliterator(), false)
                .map(Tag::getName)
                .collect(Collectors.toList());

        // Tworzymy mapowanie indeksów na cechy
        Map<Integer, String> vectorMapping = createVectorMapping(allGenres, allTags);
        printVectorMapping(vectorMapping);

        for (User user : users) {
            List<UserRating> ratings = user.getUserRatings().stream().toList();
            if (ratings.isEmpty()) continue;

            int vectorLength = allGenres.size() + allTags.size();
            double[] userVector = new double[vectorLength];

            for (UserRating rating : ratings) {
                UUID movieId = rating.getMovie().getId();
                double ratingValue = rating.getRating();

                movieEmbeddingRepository.findByMovie_Id(movieId).ifPresent(me -> {
                    double[] movieVector = me.getEmbeddingVector();
                    for (int i = 0; i < movieVector.length; i++) {
                        double weight = i < allGenres.size() ? GENRE_WEIGHT : TAG_WEIGHT;
                        userVector[i] += movieVector[i] * ratingValue * weight;
                    }
                });
            }

            // Normalizacja wektora
            double sum = Arrays.stream(userVector).sum();
            if (sum > 0) {
                for (int i = 0; i < userVector.length; i++) {
                    userVector[i] /= sum;
                }
            }

            saveUserVector(user, userVector, vectorMapping);
            System.out.println("User: " + user.getEmail() + ", Embedding: " + Arrays.toString(userVector));
        }
    }

    private Map<Integer, String> createVectorMapping(List<String> allGenres, List<String> allTags) {
        Map<Integer, String> mapping = new LinkedHashMap<>();
        for (int i = 0; i < allGenres.size(); i++) {
            mapping.put(i, "Genre: " + allGenres.get(i));
        }
        for (int i = 0; i < allTags.size(); i++) {
            mapping.put(allGenres.size() + i, "Tag: " + allTags.get(i));
        }
        return mapping;
    }

    private void printVectorMapping(Map<Integer, String> mapping) {
        System.out.println("=== USER VECTOR INDEX MAPPING ===");
        mapping.forEach((index, feature) -> System.out.println("Index " + index + " -> " + feature));
    }

    private void saveUserVector(User user, double[] userVector, Map<Integer, String> vectorMapping) {
        UserEmbedding ue = new UserEmbedding();
        ue.setUser(user);
        ue.setModelVersion("v1");
        ue.setEmbeddingVector(userVector);
        ue.setFeatureMapping(vectorMapping);
        ue.setCreatedAt(OffsetDateTime.now());
        ue.setUpdatedAt(OffsetDateTime.now());
        userEmbeddingRepository.save(ue);
    }

    public boolean hasPreferences(UUID userId) {
        return userEmbeddingRepository.existsByUser_Id(userId);
    }

    private int getEmbeddingLength() {
        return movieEmbeddingRepository.findFirstByOrderByCreatedAtDesc()
                .map(me -> me.getEmbeddingVector().length)
                .orElse(0);
    }

    public void createUserEmbeddingFromSurvey(UUID userId, SurveyRequest surveyRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Pobieramy wszystkie gatunki i tagi
        List<String> allGenres = StreamSupport.stream(genreRepository.findAll().spliterator(), false)
                .map(g -> g.getName())
                .collect(Collectors.toList());

        List<String> allTags = StreamSupport.stream(tagRepository.findAll().spliterator(), false)
                .map(Tag::getName)
                .collect(Collectors.toList());

        Map<Integer, String> vectorMapping = createVectorMapping(allGenres, allTags);

        int vectorLength = allGenres.size() + allTags.size();
        double[] userVector = new double[vectorLength];

        // Uwzględniamy oceny filmów z ankiety
        if (surveyRequest.getRatings() != null) {
            for (SurveyRequest.MovieRating rating : surveyRequest.getRatings()) {
                UUID movieId = rating.getMovieId();
                double ratingValue = rating.getRating();

                movieEmbeddingRepository.findByMovie_Id(movieId).ifPresent(me -> {
                    double[] movieVector = me.getEmbeddingVector();
                    for (int i = 0; i < movieVector.length; i++) {
                        double weight = i < allGenres.size() ? GENRE_WEIGHT : TAG_WEIGHT;
                        userVector[i] += movieVector[i] * ratingValue * weight;
                    }
                });
            }
        }

        // Podbijamy ulubione gatunki
        if (surveyRequest.getFavouriteGenres() != null) {
            for (String favGenre : surveyRequest.getFavouriteGenres()) {
                int index = allGenres.indexOf(favGenre);
                if (index >= 0) {
                    userVector[index] += GENRE_WEIGHT * FAVOURITE_GENRES_MULTIPLIER;
                }
            }
        }

        // Normalizacja wektora
        double sum = Arrays.stream(userVector).sum();
        if (sum > 0) {
            for (int i = 0; i < userVector.length; i++) {
                userVector[i] /= sum;
            }
        }

        // Zapisujemy embedding użytkownika
        saveUserVector(user, userVector, vectorMapping);
        System.out.println("User (from survey): " + user.getEmail() + ", Embedding: " + Arrays.toString(userVector));
    }
}

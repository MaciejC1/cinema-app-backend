package com.project.cinemabackend.service;

import com.project.cinemabackend.model.MovieEmbedding;
import com.project.cinemabackend.model.User;
import com.project.cinemabackend.model.UserEmbedding;
import com.project.cinemabackend.model.UserRating;
import com.project.cinemabackend.repository.MovieEmbeddingRepository;
import com.project.cinemabackend.repository.UserEmbeddingRepository;
import com.project.cinemabackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserEmbeddingService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieEmbeddingRepository movieEmbeddingRepository;
    @Autowired
    private UserEmbeddingRepository userEmbeddingRepository;

    private static final double GENRE_WEIGHT = 2.0;
    private static final double TAG_WEIGHT = 1.0;
    private static final int NUM_GENRES = 19;

    public void generateUserVectors() {
        List<User> users = (List<User>) userRepository.findAll();

        for (User user : users) {
            List<UserRating> ratings = user.getUserRatings().stream().toList();
            if (ratings.isEmpty()) continue;

            int vectorLength = getEmbeddingLength();
            double[] userVector = new double[vectorLength];

            for (UserRating rating : ratings) {
                UUID movieId = rating.getMovie().getId();
                double ratingValue = rating.getRating();

                Optional<MovieEmbedding> optionalEmbedding = movieEmbeddingRepository.findByMovie_Id(movieId);

                if (optionalEmbedding.isPresent()) {
                    MovieEmbedding embedding = optionalEmbedding.get();
                    double[] movieVector = embedding.getEmbeddingVector();

                    for (int i = 0; i < movieVector.length; i++) {
                        if (i < NUM_GENRES) {
                            userVector[i] += movieVector[i] * ratingValue * GENRE_WEIGHT;
                        } else {
                            userVector[i] += movieVector[i] * ratingValue * TAG_WEIGHT;
                        }
                    }
                }
            }

            double sum = Arrays.stream(userVector).sum();
            if (sum > 0) {
                for (int i = 0; i < userVector.length; i++) {
                    userVector[i] /= sum;
                }
            }
            saveUserVector(user, userVector);
            System.out.println("User: " + user.getEmail() + ", Embedding: " + Arrays.toString(userVector));
        }
    }

    private void saveUserVector(User user, double[] userVector) {
        UserEmbedding userEmbedding = new UserEmbedding();
        userEmbedding.setUser(user);
        userEmbedding.setModelVersion("v1");
        userEmbedding.setEmbeddingVector(userVector);
        userEmbedding.setCreatedAt(OffsetDateTime.now());
        userEmbedding.setUpdatedAt(OffsetDateTime.now());
        userEmbeddingRepository.save(userEmbedding);
    }

    private int getEmbeddingLength() {
        Optional<MovieEmbedding> optional = movieEmbeddingRepository.findFirstByOrderByCreatedAtDesc();
        return optional.map(e -> e.getEmbeddingVector().length).orElse(0);
    }
}

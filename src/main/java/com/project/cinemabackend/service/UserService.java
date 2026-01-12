package com.project.cinemabackend.service;

import com.project.cinemabackend.dto.GenrePreferenceDto;
import com.project.cinemabackend.dto.UserDTO;
import com.project.cinemabackend.model.User;
import com.project.cinemabackend.model.UserEmbedding;
import com.project.cinemabackend.repository.UserEmbeddingRepository;
import com.project.cinemabackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserEmbeddingRepository userEmbeddingRepository;

    public UserService(UserRepository userRepository, UserEmbeddingRepository userEmbeddingRepository) {
        this.userRepository = userRepository;
        this.userEmbeddingRepository = userEmbeddingRepository;
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Użytkownik nie znaleziony"));

        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .toList();

        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), roles);
    }


    public List<GenrePreferenceDto> getFavoriteGenresForUser(
            double[] userVector,
            Map<Integer, String> indexToFeature
    ) {
        List<Map.Entry<String, Double>> genres = new ArrayList<>();

        for (int i = 0; i < userVector.length; i++) {
            String feature = indexToFeature.get(i);

            if (feature != null && feature.startsWith("Genre:")) {
                double value = userVector[i];

                if (value > 0) {
                    String genreName = feature.replace("Genre:", "").trim();
                    genres.add(Map.entry(genreName, value));
                }
            }
        }

        if (genres.isEmpty()) {
            return List.of();
        }

        genres.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<Map.Entry<String, Double>> topGenres =
                genres.stream().limit(6).toList();

        double maxValue = topGenres.get(0).getValue();

        return topGenres.stream()
                .map(entry -> new GenrePreferenceDto(
                        entry.getKey(),
                        (int) Math.round((entry.getValue() / maxValue) * 100)
                ))
                .toList();
    }

    public List<GenrePreferenceDto> getFavoriteGenresForUser(UUID userId) {

        UserEmbedding embedding = userEmbeddingRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User embedding not found for userId: " + userId
                        ));

        return getFavoriteGenresForUser(
                embedding.getEmbeddingVector(),
                embedding.getFeatureMapping()
        );
    }

}

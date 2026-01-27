package com.project.cinemabackend.service;

import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.MovieEmbedding;
import com.project.cinemabackend.model.Tag;
import com.project.cinemabackend.repository.GenreRepository;
import com.project.cinemabackend.repository.MovieEmbeddingRepository;
import com.project.cinemabackend.repository.MovieRepository;
import com.project.cinemabackend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieEmbeddingService {

    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private MovieEmbeddingRepository movieEmbeddingRepository;

    public void generateEmbeddingsForAllMovies() {
        List<String> allGenres = StreamSupport.stream(genreRepository.findAll().spliterator(), false)
                .map(g -> g.getName())
                .collect(Collectors.toList());

        List<String> allTags = StreamSupport.stream(tagRepository.findAll().spliterator(), false)
                .map(Tag::getName)
                .collect(Collectors.toList());

        Map<Integer, String> vectorMapping = createVectorMapping(allGenres, allTags);

        printVectorMapping(vectorMapping);

        List<Movie> movies = StreamSupport.stream(movieRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        for (Movie movie : movies) {
            double[] embedding = generateEmbeddingForMovie(movie, allGenres, allTags);
            saveEmbeddingToDatabase(movie, embedding, vectorMapping);
            System.out.println("Movie: " + movie.getId() + " -> " + Arrays.toString(embedding));
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
        System.out.println("=== VECTOR INDEX MAPPING ===");
        mapping.forEach((index, feature) -> System.out.println("Index " + index + " -> " + feature));
    }

    private double[] generateEmbeddingForMovie(Movie movie, List<String> allGenres, List<String> allTags) {
        int size = allGenres.size() + allTags.size();
        double[] vector = new double[size];

        for (var mg : movie.getMovieGenres()) {
            String genreName = mg.getGenre().getName();
            int index = allGenres.indexOf(genreName);
            if (index >= 0) vector[index] = 1.0;
        }

        for (var mt : movie.getMovieTags()) {
            String tagName = mt.getTag().getName();
            int index = allTags.indexOf(tagName);
            if (index >= 0) vector[allGenres.size() + index] = 1.0;
        }

        return vector;
    }

    private void saveEmbeddingToDatabase(Movie movie, double[] embedding, Map<Integer, String> vectorMapping) {
        MovieEmbedding me = new MovieEmbedding();
        me.setMovie(movie);
        me.setModelVersion("v1");
        me.setEmbeddingVector(embedding);
        me.setFeatureMapping(vectorMapping);
        me.setCreatedAt(OffsetDateTime.now());
        me.setUpdatedAt(OffsetDateTime.now());
        movieEmbeddingRepository.save(me);
    }
}

package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.MediaDTO;
import com.project.cinemabackend.dto.MovieDTO;
import com.project.cinemabackend.dto.MovieDetailsDTO;
import com.project.cinemabackend.dto.MovieMinimalDTO;
import com.project.cinemabackend.model.Media;
import com.project.cinemabackend.model.Movie;
import com.project.cinemabackend.model.MovieDirector;
import com.project.cinemabackend.model.MovieGenre;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper()
public interface MovieMapper {

    @Mapping(target = "directors", expression = "java(mapDirectors(movie.getMovieDirectors()))")
    @Mapping(target = "genres", expression = "java(mapGenres(movie.getMovieGenres()))")
    @Mapping(target = "backdrop", expression = "java(mapBackdrop(movie.getMedia()))")
    @Mapping(target = "poster", expression = "java(mapPoster(movie.getMedia()))")
    MovieDTO toDto(Movie movie);
    List<MovieDTO> toDtoList(List<Movie> movies);

    @Mapping(target = "directors", expression = "java(mapDirectors(movie.getMovieDirectors()))")
    @Mapping(target = "genres", expression = "java(mapGenres(movie.getMovieGenres()))")
    @Mapping(target = "media", expression = "java(mapMedia(movie.getMedia()))")
    MovieDetailsDTO toDetailsDto(Movie movie);

    @Mapping(target = "poster", expression = "java(mapPoster(movie.getMedia()))")
    MovieMinimalDTO toMinimalDto(Movie movie);
    List<MovieMinimalDTO> toMinimalDtoList(List<Movie> movies);

    default List<String> mapDirectors(Set<MovieDirector> directors) {
        return directors.stream()
                .map(d -> d.getDirector().getName())
                .toList();
    }

    default List<String> mapGenres(Set<MovieGenre> genres) {
        return genres.stream()
                .map(g -> g.getGenre().getName())
                .toList();
    }

    default String mapBackdrop(Set<Media> media) {
        return media.stream()
                .filter(m -> "backdrop".equals(m.getMediaType()))
                .map(Media::getUrl)
                .findFirst()
                .orElse("");
    }

    default List<MediaDTO> mapMedia(Set<Media> media) {
        return media.stream()
                .map(m -> new MediaDTO(m.getUrl(), m.getMediaType()))
                .toList();
    }

    default String mapPoster(Set<Media> media) {
        return media.stream()
                .filter(m -> "poster".equals(m.getMediaType()))
                .map(Media::getUrl)
                .findFirst()
                .orElse("");
    }
}



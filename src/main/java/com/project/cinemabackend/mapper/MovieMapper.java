package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.*;
import com.project.cinemabackend.dto.ai.MovieAiDTO;
import com.project.cinemabackend.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(uses = {ShowtimeMapper.class})
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


    @Mapping(target = "poster", expression = "java(mapPoster(movie.getMedia()))")
    @Mapping(target = "genres", expression = "java(mapGenres(movie.getMovieGenres()))")
    @Mapping(target = "showtimes", source = "showtimes")
    MovieShowtimesDTO toMoviesShowtimesDto(Movie movie);
    List<MovieShowtimesDTO> toMoviesShowtimesListDto(List<Movie> movies);

    @Mapping(target = "genres", expression = "java(mapGenres(movie.getMovieGenres()))")
    @Mapping(target = "tags", expression = "java(mapTags(movie.getMovieTags()))")
    MovieAiDTO toMovieAiDto(Movie movie);
    List<MovieAiDTO> toMovieAiDtoList(List<Movie> movies);

    @Mapping(target = "poster", expression = "java(mapPoster(movie.getMedia()))")
    MovieSurveyDTO toMovieSurveyDto(Movie movie);
    List<MovieSurveyDTO> toMovieSurveyDtoList(List<Movie> movies);

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

    default List<String> mapTags(Set<MovieTag> tags) {
        return tags.stream()
                .map(t -> t.getTag().getName())
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
package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.ai.MovieAiRatingDTO;
import com.project.cinemabackend.model.UserRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {MovieMapper.class})
public interface RatingMapper {
    @Mapping(target = "movieAiDTO", source = "movie")
    MovieAiRatingDTO toMovieAiRatingDto(UserRating userRating);
    List<MovieAiRatingDTO> toMovieAiRatingDtoList(List<UserRating> userRatings);
}

package com.project.cinemabackend.security;

import com.project.cinemabackend.mapper.*;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public MovieMapper movieMapper() {
        return Mappers.getMapper(MovieMapper.class);
    }

    @Bean
    public CinemaMapper cinemaMapper() {return  Mappers.getMapper(CinemaMapper.class);}

    @Bean
    public UserMapper userMapper() {return  Mappers.getMapper(UserMapper.class);}

    @Bean
    public ShowtimeMapper showtimeMapper() {return Mappers.getMapper(ShowtimeMapper.class);}
}


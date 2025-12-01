package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.auth.RegisterRequest;
import com.project.cinemabackend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper()
public interface UserMapper {
    @Mapping(target = "preferredCinema", ignore = true)
    User toEntity(RegisterRequest registerRequest);
}

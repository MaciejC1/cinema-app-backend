package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.ai.TagMinimalDTO;
import com.project.cinemabackend.model.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper()
public interface MovieTagMapper {
    TagMinimalDTO toTagMinimalDto(Tag tag);
    List<TagMinimalDTO> toTagMinimalDtoList(List<Tag> tags);
}

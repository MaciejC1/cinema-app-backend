package com.project.cinemabackend.mapper;

import com.project.cinemabackend.dto.ai.TagMinimalDTO;
import com.project.cinemabackend.model.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper()
public interface MovieTagMapper {
    TagMinimalDTO toTagMinimalDTO(Tag tag);
    List<TagMinimalDTO> toTagMinimalDTOList(List<Tag> tags);
}

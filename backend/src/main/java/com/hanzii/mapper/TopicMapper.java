package com.hanzii.mapper;

import com.hanzii.dto.response.TopicResponse;
import com.hanzii.entity.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    @Mapping(target = "vocabularyCount", ignore = true)
    TopicResponse toResponse(Topic topic);
    List<TopicResponse> toResponseList(List<Topic> topics);
}

package com.hanzii.mapper;

import com.hanzii.dto.response.VocabularyResponse;
import com.hanzii.entity.Vocabulary;
import com.hanzii.entity.enums.LearningStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface VocabularyMapper {

    @Mapping(source = "topic.id", target = "topicId")
    @Mapping(source = "topic.name", target = "topicName")
    @Mapping(target = "status", ignore = true)
    VocabularyResponse toResponse(Vocabulary vocabulary);

    default VocabularyResponse toResponseWithStatus(Vocabulary vocabulary, LearningStatus status) {
        VocabularyResponse response = toResponse(vocabulary);
        response.setStatus(status != null ? status : LearningStatus.NEW);
        return response;
    }
}

package com.hanzii.service;

import com.hanzii.dto.response.TopicResponse;
import com.hanzii.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    @Transactional(readOnly = true)
    public List<TopicResponse> getAllTopics() {
        Map<Long, Long> countsByTopic = topicRepository.countVocabulariesByTopic().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return topicRepository.findAll().stream()
                .map(topic -> TopicResponse.builder()
                        .id(topic.getId())
                        .name(topic.getName())
                        .description(topic.getDescription())
                        .vocabularyCount(countsByTopic.getOrDefault(topic.getId(), 0L))
                        .build())
                .toList();
    }
}

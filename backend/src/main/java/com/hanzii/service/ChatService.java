package com.hanzii.service;

import com.hanzii.dto.request.ChatRequest;
import com.hanzii.dto.response.ChatResponse;
import com.hanzii.dto.response.ConversationResponse;
import com.hanzii.entity.Conversation;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.mapper.ConversationMapper;
import com.hanzii.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatPersistenceService chatPersistenceService;
    private final AiTutorTaskService aiTutorTaskService;
    private final ConversationMapper conversationMapper;

    public ChatResponse sendMessage(Long userId, ChatRequest request) {
        Conversation conversation = chatPersistenceService.saveUserMessage(
                userId, request.getConversationId(), request.getMessage());
        List<Map<String, String>> history = chatPersistenceService.buildHistory(conversation.getId());
        String aiContent = aiTutorTaskService.chatAsync(request.getMessage(), history).join();
        chatPersistenceService.saveAiMessage(userId, conversation.getId(), request.getMessage(), aiContent);

        return ChatResponse.builder()
                .conversationId(conversation.getId())
                .userMessage(request.getMessage())
                .aiMessage(aiContent)
                .build();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public List<ConversationResponse> getConversations(Long userId) {
        return conversationMapper.toResponseList(
                conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId));
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public ConversationResponse getConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        return conversationMapper.toResponse(conversation);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        conversationRepository.delete(conversation);
    }
}

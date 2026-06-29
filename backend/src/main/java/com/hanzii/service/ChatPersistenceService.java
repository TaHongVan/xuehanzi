package com.hanzii.service;

import com.hanzii.entity.Conversation;
import com.hanzii.entity.Message;
import com.hanzii.entity.enums.MessageSender;
import com.hanzii.exception.ResourceNotFoundException;
import com.hanzii.repository.ConversationRepository;
import com.hanzii.repository.MessageRepository;
import com.hanzii.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatPersistenceService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Conversation saveUserMessage(Long userId, Long conversationId, String content) {
        Conversation conversation = resolveConversation(userId, conversationId);
        Message userMsg = Message.builder()
                .conversation(conversation)
                .sender(MessageSender.USER)
                .content(content)
                .build();
        messageRepository.save(userMsg);
        return conversation;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
    public List<Map<String, String>> buildHistory(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<Map<String, String>> history = new ArrayList<>();
        for (Message msg : messages) {
            history.add(Map.of(
                    "role", msg.getSender() == MessageSender.USER ? "user" : "assistant",
                    "content", msg.getContent()
            ));
        }
        return history;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void saveAiMessage(Long userId, Long conversationId, String userMessage, String aiContent) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Message aiMsg = Message.builder()
                .conversation(conversation)
                .sender(MessageSender.AI)
                .content(aiContent)
                .build();
        messageRepository.save(aiMsg);

        if (conversation.getTitle() == null) {
            conversation.setTitle(truncate(userMessage, 50));
            conversationRepository.save(conversation);
        }
    }

    private Conversation resolveConversation(Long userId, Long conversationId) {
        if (conversationId != null) {
            return conversationRepository.findByIdAndUserId(conversationId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        }
        Conversation conversation = new Conversation();
        conversation.setUser(userRepository.getReferenceById(userId));
        conversation.setMessages(new ArrayList<>());
        return conversationRepository.save(conversation);
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}

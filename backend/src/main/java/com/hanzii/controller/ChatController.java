package com.hanzii.controller;

import com.hanzii.dto.request.ChatRequest;
import com.hanzii.dto.response.ApiResponse;
import com.hanzii.dto.response.ChatResponse;
import com.hanzii.dto.response.ConversationResponse;
import com.hanzii.security.SecurityUtils;
import com.hanzii.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.sendMessage(SecurityUtils.getCurrentUserId(), request)));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations() {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getConversations(SecurityUtils.getCurrentUserId())));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getConversation(SecurityUtils.getCurrentUserId(), id)));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", null));
    }
}

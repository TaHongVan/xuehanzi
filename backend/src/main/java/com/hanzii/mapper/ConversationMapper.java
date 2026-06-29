package com.hanzii.mapper;

import com.hanzii.dto.response.ConversationResponse;
import com.hanzii.entity.Conversation;
import com.hanzii.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(source = "messages", target = "messages")
    ConversationResponse toResponse(Conversation conversation);

    ConversationResponse.MessageResponse toMessageResponse(Message message);

    List<ConversationResponse> toResponseList(List<Conversation> conversations);
}

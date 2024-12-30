package com.qring.message.application.v1.res;

import com.qring.message.domain.model.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageGetByIdResDTOV1 {

    private Message message;

    public static MessageGetByIdResDTOV1 of(MessageEntity messageEntity) {
        return MessageGetByIdResDTOV1.builder()
                .message(Message.from(messageEntity))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private Long slackId;
        private Long userId;
        private String content;

        public static Message from(MessageEntity messageEntity) {
            return Message.builder()
                    .slackId(messageEntity.getId())
                    .userId(messageEntity.getUserId())
                    .content(messageEntity.getContent())
                    .build();
        }
    }
}

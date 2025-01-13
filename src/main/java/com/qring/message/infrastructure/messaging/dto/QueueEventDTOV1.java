package com.qring.message.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEventDTOV1 {

    private Long userId;
    private String slackEmail;
    private String username;

    public static QueueEventDTOV1 from(Long userId, String slackEmail, String username) {
        return QueueEventDTOV1.builder()
                .userId(userId)
                .slackEmail(slackEmail)
                .username(username)
                .build();
    }
}

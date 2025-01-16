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
    private String username;
    private String slackEmail;

    public static QueueEventDTOV1 from(Long userId, String username, String slackEmail) {
        return QueueEventDTOV1.builder()
                .userId(userId)
                .username(username)
                .slackEmail(slackEmail)
                .build();
    }
}

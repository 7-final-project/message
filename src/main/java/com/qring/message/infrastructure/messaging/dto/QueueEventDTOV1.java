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

    private User user;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        private Long id;
        private String username;
        private String slackEmail;

        public static User from(Long id, String username, String slackEmail) {
            return User.builder()
                    .id(id)
                    .username(username)
                    .slackEmail(slackEmail)
                    .build();
        }
    }
}

package com.qring.message.application.v1.res;

import com.qring.message.domain.model.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchResDTOV1 {

    private MessagePage messagePage;

    public static MessageSearchResDTOV1 of(Page<MessageEntity> messageEntityPage) {
        return MessageSearchResDTOV1.builder()
                .messagePage(MessagePage.from(messageEntityPage))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessagePage {

        private List<Message> content;
        private PageDetails page;

        public static MessagePage from(Page<MessageEntity> messageEntityPage) {
            return MessagePage.builder()
                    .content(Message.from(messageEntityPage.getContent()))
                    .page(PageDetails.from(messageEntityPage))
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

            public static List<Message> from(List<MessageEntity> messageEntityList) {
                return messageEntityList.stream()
                        .map(Message::from)
                        .toList();
            }

            public static Message from(MessageEntity messageEntity) {

                return Message.builder()
                        .slackId(messageEntity.getId())
                        .userId(messageEntity.getUserId())
                        .content(messageEntity.getContent())
                        .build();
            }
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PageDetails {

            private int size;
            private int number;
            private long totalElements;
            private int totalPages;

            public static PageDetails from(Page<MessageEntity> messageEntityPage) {
                return PageDetails.builder()
                        .size(messageEntityPage.getSize())
                        .number(messageEntityPage.getNumber())
                        .totalElements(messageEntityPage.getTotalElements())
                        .totalPages(messageEntityPage.getTotalPages())
                        .build();
            }
        }
    }
}

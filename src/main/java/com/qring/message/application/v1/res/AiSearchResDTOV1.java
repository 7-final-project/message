package com.qring.message.application.v1.res;

import com.qring.message.domain.model.AiEntity;
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
public class AiSearchResDTOV1 {

    private AiPage aiPage;

    public static AiSearchResDTOV1 of(Page<AiEntity> aiEntityPage) {
        return AiSearchResDTOV1.builder()
                .aiPage(AiPage.from(aiEntityPage))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiPage {

        private List<Ai> content;
        private PageDetails page;

        public static AiPage from(Page<AiEntity> aiEntityPage) {
            return AiPage.builder()
                    .content(Ai.from(aiEntityPage.getContent()))
                    .page(PageDetails.from(aiEntityPage))
                    .build();
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Ai {

            private Long aiId;
            private String question;
            private String answer;

            public static List<Ai> from(List<AiEntity> aiEntityList) {
                return aiEntityList.stream()
                        .map(Ai::from)
                        .toList();
            }

            public static Ai from(AiEntity aiEntity) {

                return Ai.builder()
                        .aiId(aiEntity.getId())
                        .question(aiEntity.getQuestion())
                        .answer(aiEntity.getAnswer())
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

            public static PageDetails from(Page<AiEntity> aiEntityPage) {
                return PageDetails.builder()
                        .size(aiEntityPage.getSize())
                        .number(aiEntityPage.getNumber())
                        .totalElements(aiEntityPage.getTotalElements())
                        .totalPages(aiEntityPage.getTotalPages())
                        .build();
            }
        }
    }
}

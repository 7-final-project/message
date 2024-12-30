package com.qring.message.application.v1.res;

import com.qring.message.domain.model.AiEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPostResDTOV1 {

    private Ai ai;

    public static AiPostResDTOV1 of(AiEntity aiEntity) {
        return AiPostResDTOV1.builder()
                .ai(Ai.from(aiEntity))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ai {

        private Long id;
        private String question;
        private String answer;

        public static Ai from(AiEntity aiEntity) {
            return Ai.builder()
                    .id(aiEntity.getId())
                    .question(aiEntity.getQuestion())
                    .answer(aiEntity.getAnswer())
                    .build();
        }
    }
}

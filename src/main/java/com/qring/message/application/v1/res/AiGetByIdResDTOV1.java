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
public class AiGetByIdResDTOV1 {

    private Ai ai;

    public static AiGetByIdResDTOV1 of(AiEntity aiEntity) {
        return AiGetByIdResDTOV1.builder()
                .ai(Ai.from(aiEntity))
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

        public static Ai from(AiEntity aiEntity) {
            return Ai.builder()
                    .aiId(aiEntity.getId())
                    .question(aiEntity.getQuestion())
                    .answer(aiEntity.getAnswer())
                    .build();
        }
    }
}

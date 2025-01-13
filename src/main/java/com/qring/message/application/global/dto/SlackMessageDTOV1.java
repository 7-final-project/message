package com.qring.message.application.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackMessageDTOV1 {

    private String channel;
    private List<Block> blocks;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Block {
        private String type;
        private String block_id;
        private Text text;
        private List<ContextElement> elements;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Text {
        private String type;
        private String text;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextElement {
        private String type;
        private String text;
    }
}

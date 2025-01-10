package com.qring.message.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.message.application.v1.service.MessageServiceV1;
import com.qring.message.application.v1.service.SlackServiceV1;
import com.qring.message.infrastructure.messaging.dto.ReservationAndQueueEventDTOV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MessageService - KafkaMessageConsumerV1 Log")
public class KafkaMessageConsumerV1 {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final SlackServiceV1 slackServiceV1;
    private final MessageServiceV1 messageServiceV1;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 대기 5번째 순번인 고객에게 메시지 전송
     */
    @KafkaListener(topics = "userinfo-send-event-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void sendMessageToUser(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);

            Long userId = rootNode.get("userId").asLong();
            String slackEmail = rootNode.get("slackEmail").asText();
            String username = rootNode.get("username").asText();

            String slackId = slackServiceV1.extractSlackIdByEmail(slackEmail); // 슬랙 이메일로 Slack ID 조회

            // 메시지 페이로드 생성
            String content = createPayload(slackId, username);

            // 메세지 DB에 저장
            messageServiceV1.postBy(userId, content);

            // 웹훅 URL로 메시지 전송
            slackServiceV1.sendRequest(slackWebhookUrl, content);

            log.info("Message sent successfully to user with email: {}", slackEmail);
        } catch (Exception e) {
            log.error("Error occurred while sending message to user: {}", e.getMessage(), e);
        }
    }

    /**
     * 메시지 페이로드 생성
     */
    private String createPayload(String slackId, String username) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> payload = Map.of(
                "channel", slackId,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "block_id", "section-0",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", String.format("%s 님의 대기 순번이 5번째로 다가왔습니다!\n\n 가게 앞에서 대기해주세요.", username)
                                )
                        ),
                        Map.of("type", "divider"),
                        Map.of(
                                "type", "context",
                                "elements", List.of(
                                        Map.of(
                                                "type", "mrkdwn",
                                                "text", "원격줄서기, 즉시예약\n스마트외식, 큐링!"
                                        )
                                )
                        )
                )
        );

        return objectMapper.writeValueAsString(payload);
    }

    @KafkaListener(topics = "queue-reservation-event-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void sendReservationMessageToUser(String message) {
        try {
            String slackEmail = slackServiceV1.extractSlackEmailFromReservationMessage(message);
            String slackId = slackServiceV1.extractSlackIdByEmail(slackEmail);

            ReservationAndQueueEventDTOV1 dto = slackServiceV1.parseMessage(message);

            String payload = slackServiceV1.createPayload(slackId, dto);

            slackServiceV1.sendRequest(slackWebhookUrl, payload);

            messageServiceV1.postBy(dto.getUserId(), slackServiceV1.createMessageContent(dto));

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}

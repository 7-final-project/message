package com.qring.message.application.v1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageReservationServiceV1 {

    @Value("${slack.token}")
    private String slackBotToken;

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final MessageQueueServiceV1 messageQueueServiceV1;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "queue-reservation-event-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void sendReservationMessageToUser(String message) {
        String slackEmail = extractSlackEmailFromMessage(message);
        String slackId = messageQueueServiceV1.fetchSlackIdByEmail(slackEmail);

        String payload = createPayload(slackId, message);

        messageQueueServiceV1.sendRequest(slackWebhookUrl, payload);
    }

    /**
     * 메세지에서 slackEmail 추출
     */
    private String extractSlackEmailFromMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            return rootNode.get("slackEmail").asText();
        } catch (Exception e) {
            log.error("Error parsing message to extract slack email: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }

    /**
     * 메시지 페이로드 생성
     */
    private String createPayload(String slackId, String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            // 필요한 데이터 추출
            String storeName = rootNode.get("restaurantName").asText();
            String storeTel = rootNode.get("restaurantTel").asText();
            int peopleCount = rootNode.get("headCount").asInt();
            int waitingNumber = rootNode.get("waitingNumber").asInt();
            int teamsAhead = rootNode.get("teamsAhead").asInt();

            return "{\n" +
                    "  \"channel\": \"" + slackId + "\",\n" +
                    "  \"blocks\": [\n" +
                    "    {\n" +
                    "      \"type\": \"section\",\n" +
                    "      \"text\": {\n" +
                    "        \"type\": \"mrkdwn\",\n" +
                    "        \"text\": \"고객님께서는 대기 명단에 정상적으로 접수 되셨습니다.\\n변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다.\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"section\",\n" +
                    "      \"text\": {\n" +
                    "        \"type\": \"mrkdwn\",\n" +
                    "        \"text\": \"■ *매장명*: " + storeName + "\\n" +
                    "■ *매장 전화번호*: " + storeTel + "\\n" +
                    "■ *인원*: " + peopleCount + "명\\n" +
                    "■ *대기번호*: " + waitingNumber + "번\\n" +
                    "■ *내 앞 대기팀*: " + teamsAhead + "팀\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"divider\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"type\": \"context\",\n" +
                    "      \"elements\": [\n" +
                    "        {\n" +
                    "          \"type\": \"mrkdwn\",\n" +
                    "          \"text\": \"원격줄서기, 즉시예약\\n스마트외식, 큐링!\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        } catch (Exception e) {
            log.error("Error parsing message to extract slack email: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }
}
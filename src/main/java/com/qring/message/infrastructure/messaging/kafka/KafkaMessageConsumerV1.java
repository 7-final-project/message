package com.qring.message.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.message.application.v1.service.SlackServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MessageService - KafkaMessageConsumerV1 Log")
public class KafkaMessageConsumerV1 {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final SlackServiceV1 slackServiceV1;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 대기 5번째 순번인 고객에게 메시지 전송
     */
    @KafkaListener(topics = "userslackemail-send-event-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void sendMessageToUser(String message) {
        try {
            String slackEmail = slackServiceV1.extractSlackEmailFromMessage(message);
            String slackId = slackServiceV1.fetchSlackIdByEmail(slackEmail); // 슬랙 이메일로 Slack ID 조회

            // 메시지 페이로드 생성
            String payload = createPayload(slackId);

            // 웹훅 URL로 메시지 전송
            slackServiceV1.sendRequest(slackWebhookUrl, payload);

            log.info("Message sent successfully to user with email: {}", slackEmail);
        } catch (Exception e) {
            log.error("Error occurred while sending message to user: {}", e.getMessage(), e);
        }
    }

    /**
     * 메시지 페이로드 생성
     */
    private String createPayload(String slackId) {
        // Block Kit을 포함하여 메시지 포맷을 작성
        return "{\n" +
                "  \"channel\": \"" + slackId + "\",\n" +
                "   \"blocks\": [\n" +
                "    {\n" +
                "      \"type\": \"section\",\n" +
                "      \"block_id\": \"section-0\",\n" +
                "      \"text\": {\n" +
                "        \"type\": \"mrkdwn\",\n" +
                "        \"text\": \"고객님의 대기 순번이 5번째로 다가왔습니다! 가게 앞에서 대기해주세요.\"\n" +
                "      }\n" +
                "    },\n" +
                "  ]\n" +
                "}";
    }

    @KafkaListener(topics = "queue-reservation-event-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void sendReservationMessageToUser(String message) {
        String slackEmail = slackServiceV1.extractSlackEmailFromReservationMessage(message);
        String slackId = slackServiceV1.fetchSlackIdByEmail(slackEmail);

        String payload = createPayload(slackId, message);

        slackServiceV1.sendRequest(slackWebhookUrl, payload);
    }

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

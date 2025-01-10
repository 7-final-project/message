package com.qring.message.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.message.application.global.dto.SlackMessageDTOV1;
import com.qring.message.application.v1.service.MessageServiceV1;
import com.qring.message.application.v1.service.SlackServiceV1;
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
    public void sendReservationMessageToUser(String message) throws JsonProcessingException {
        String slackEmail = slackServiceV1.extractSlackEmailFromReservationMessage(message);
        String slackId = slackServiceV1.extractSlackIdByEmail(slackEmail);

        JsonNode rootNode = objectMapper.readTree(message);
        // 필요한 데이터 추출
        String storeName = rootNode.get("restaurantName").asText();
        String storeTel = rootNode.get("restaurantTel").asText();
        int peopleCount = rootNode.get("headCount").asInt();
        int waitingNumber = rootNode.get("waitingNumber").asInt();
        int teamsAhead = rootNode.get("teamsAhead").asInt();
        Long userId = rootNode.get("userId").asLong();

        String content = createPayload(slackId, storeName, storeTel, peopleCount, waitingNumber, teamsAhead);

        messageServiceV1.postBy(userId, content);

        slackServiceV1.sendRequest(slackWebhookUrl, content);
    }

    private String createPayload(String slackId, String storeName, String storeTel, int peopleCount, int waitingNumber, int teamsAhead) {
        try {
            SlackMessageDTOV1 slackMessage = new SlackMessageDTOV1(
                    slackId,
                    List.of(
                            new SlackMessageDTOV1.Block(
                                    "section",
                                    null,
                                    new SlackMessageDTOV1.Text("mrkdwn",
                                            " 님께서는 대기 명단에 정상적으로 접수 되셨습니다.\n변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다."),
                                    null
                            ),
                            new SlackMessageDTOV1.Block(
                                    "section",
                                    null,
                                    new SlackMessageDTOV1.Text(
                                            "mrkdwn",
                                            "■ *매장명*: " + storeName + "\n" +
                                                    "■ *매장 전화번호*: " + storeTel + "\n" +
                                                    "■ *인원*: " + peopleCount + "명\n" +
                                                    "■ *대기번호*: " + waitingNumber + "번\n" +
                                                    "■ *내 앞 대기팀*: " + teamsAhead + "팀"
                                    ),
                                    null
                            ),
                            new SlackMessageDTOV1.Block("divider", null, null, null),
                            new SlackMessageDTOV1.Block(
                                    "context",
                                    null,
                                    null,
                                    List.of(new SlackMessageDTOV1.ContextElement("mrkdwn", "원격줄서기, 즉시예약\n스마트외식, 큐링!"))
                            )
                    )
            );

            // Java 객체를 JSON 문자열로 변환
            return objectMapper.writeValueAsString(slackMessage);
        } catch (Exception e) {
            log.error("Error parsing message to extract slack email: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }
}

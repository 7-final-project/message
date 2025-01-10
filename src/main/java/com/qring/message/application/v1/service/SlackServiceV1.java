package com.qring.message.application.v1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.message.infrastructure.messaging.dto.QueueEventDTOV1;
import com.qring.message.infrastructure.messaging.dto.ReservationAndQueueEventDTOV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackServiceV1 {

    @Value("${slack.token}")
    private String slackBotToken;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     *
     * SlackEmail에서 SlackId 추출
     */
    public String extractSlackIdByEmail(String slackEmail) {
        String url = "https://slack.com/api/users.lookupByEmail?email=" + slackEmail;
        String response = sendRequest(url, HttpMethod.GET);

        try {
            // JSON 응답을 파싱하여 Slack ID 추출
            JsonNode responseBody = objectMapper.readTree(response);
            if (responseBody.get("ok").asBoolean()) {
                return responseBody.get("user").get("id").asText();
            } else {
                String error = responseBody.get("error").asText();
                throw new RuntimeException("Failed to fetch Slack ID: " + error);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing response for fetchSlackIdByEmail: " + e.getMessage(), e);
        }
    }


    /**
     * 웹훅 URL로 POST 요청을 보내는 메서드
     */
    public void sendRequest(String url, String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("Slack response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error occurred while sending request to Slack: {}", e.getMessage(), e);
        }
    }

    /**
     * GET 요청을 보내는 메서드 (Slack ID 조회용)
     */
    public String sendRequest(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + slackBotToken);  // 슬랙 봇 토큰 추가

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
            return response.getBody();  // 응답 본문 반환
        } catch (Exception e) {
            log.error("Error occurred while sending GET request: {}", e.getMessage(), e);
            throw new RuntimeException("Error occurred while sending GET request", e);
        }
    }

    public QueueEventDTOV1 parseQueueMessage(String message) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(message);

        Long userId = rootNode.get("userId").asLong();
        String slackEmail = rootNode.get("slackEmail").asText();
        String username = rootNode.get("username").asText();

        return QueueEventDTOV1.from(
                userId,
                slackEmail,
                username
        );
    }

    public ReservationAndQueueEventDTOV1 parseMessage(String message) {
        try {
            return objectMapper.readValue(message, ReservationAndQueueEventDTOV1.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message format: " + message, e);
        }
    }

    /**
     * 메시지 페이로드 및 컨텐츠 생성
     */
    public String createQueuePayload(String slackId, String username) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> payload = Map.of(
                "channel", slackId,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "block_id", "section-0",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", String.format("%s님의 대기 순번이 5번째로 다가왔습니다!\n" +
                                                "\n " +
                                                "가게 앞에서 대기해주세요.", username)
                                )
                        ),
                        Map.of("type", "divider"),
                        Map.of(
                                "type", "context",
                                "elements", List.of(
                                        Map.of(
                                                "type", "mrkdwn",
                                                "text", "원격줄서기, 즉시예약\n" +
                                                        "스마트외식, 큐링!"
                                        )
                                )
                        )
                )
        );

        return objectMapper.writeValueAsString(payload);
    }

    public String createQueueMessageContent(QueueEventDTOV1 dto) {
        return dto.getUsername() + "님의 대기 순번이 5번째로 다가왔습니다!\\n" +
                "\\n" +
                " 가게 앞에서 대기해주세요.\n" +
                "원격줄서기, 즉시예약\n스마트외식, 큐링!";
    }

    public String createReservationPayload(String slackId, ReservationAndQueueEventDTOV1 dto) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> payload = Map.of(
                "channel", slackId,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", String.format(
                                                "%s님께서는 대기 명단에 정상적으로 접수 되셨습니다.\n변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다.",
                                                dto.getUsername()
                                        )
                                )
                        ),
                        Map.of(
                                "type", "section",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", String.format(
                                                        "■ *매장명*: %s\n" +
                                                        "■ *매장 전화번호*: %s\n" +
                                                        "■ *인원*: %d명\n" +
                                                        "■ *대기순번*: %d번",
                                                dto.getRestaurantName(),
                                                dto.getRestaurantTel(),
                                                dto.getHeadCount(),
                                                dto.getSequence()
                                        )
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

    public String createMessageContent(ReservationAndQueueEventDTOV1 dto) {
        return dto.getUsername() + "님께서는 대기 명단에 정상적으로 접수 되셨습니다.\n" +
                "변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다.\n" +
                "■ 매장명: " + dto.getRestaurantName() + "\n" +
                "■ 매장 전화번호: " + dto.getRestaurantTel() + "\n" +
                "■ 인원: " + dto.getHeadCount() + "명\n" +
                "■ 대기순번: " + dto.getSequence() + "번\n";
    }
}
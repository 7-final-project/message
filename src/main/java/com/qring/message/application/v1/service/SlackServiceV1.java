package com.qring.message.application.v1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     * 메세지에서 slackEmail 추출
     */
    public String extractSlackEmailFromMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            return rootNode.get("slackEmail").asText();  // rootNode 자체가 이메일 값이므로 asText()로 추출
        } catch (Exception e) {
            log.error("Error parsing message to extract slack email: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }

    public String extractSlackEmailFromReservationMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            return rootNode.get("slackEmail").asText();
        } catch (Exception e) {
            log.error("Error parsing message to extract slack email: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid message format", e);
        }
    }

    /**
     *
     * SlackEmail에서 SlackId 추출
     */
    public String extractSlackIdByEmail(String slackEmail) {
        String url = "https://slack.com/api/users.lookupByEmail?email=" + slackEmail;
        String response = sendRequest(url, HttpMethod.GET);

        try {
            // JSON 응답을 파싱하여 Slack ID 추출
            var responseBody = objectMapper.readTree(response);
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

    public String createMessageContent(ReservationAndQueueEventDTOV1 dto) {
        return dto.getUsername() + "님께서는 대기 명단에 정상적으로 접수 되셨습니다.\n" +
                "변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다.\n" +
                "■ 매장명: " + dto.getRestaurantName() + "\n" +
                "■ 매장 전화번호: " + dto.getRestaurantTel() + "\n" +
                "■ 인원: " + dto.getHeadCount() + "명\n" +
                "■ 대기순번: " + dto.getSequence() + "번\n";
    }

    public String createPayload(String slackId, ReservationAndQueueEventDTOV1 dto) throws Exception {

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
                                                "■ *매장명*: %s\n■ *매장 전화번호*: %s\n■ *인원*: %d명\n■ *대기순번*: %d번",
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

    public ReservationAndQueueEventDTOV1 parseMessage(String message) {
        try {
            return objectMapper.readValue(message, ReservationAndQueueEventDTOV1.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message format: " + message, e);
        }
    }
}
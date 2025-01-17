package com.qring.message.application.v1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.message.application.global.exception.ErrorCode;
import com.qring.message.application.global.exception.MessageException;
import com.qring.message.infrastructure.messaging.dto.CreateReservationMessageDTOV1;
import com.qring.message.infrastructure.messaging.dto.QueueEventDTOV1;
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

    // NOTE: SlackEmail에서 SlackId 추출
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
                throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "SlackID 추출 실패: " + error);
            }
        } catch (Exception e) {
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "SlackEmail 추출 실패: " + e.getMessage());
        }
    }


    // NOTE: 웹훅 URL로 POST 요청을 보내는 메서드
    public void sendRequest(String url, String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("Slack 응답 객체: {}", response.getBody());
        } catch (Exception e) {
            log.error("메세지 요청 실패: {}", e.getMessage(), e);
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "메세지 요청 실패");
        }
    }


    // NOTE: GET 요청을 보내는 메서드 (Slack ID 조회용)
    public String sendRequest(String url, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + slackBotToken);  // 슬랙 봇 토큰 추가

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
            return response.getBody();  // 응답 본문 반환
        } catch (Exception e) {
            log.error("메세지 가져오기 실패: {}", e.getMessage(), e);
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "메세지 가져오기 실패");
        }
    }

    public QueueEventDTOV1 parseQueueMessage(String message) {
        try {
            return objectMapper.readValue(message, QueueEventDTOV1.class);
        } catch (Exception e) {
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "유효하지 않은 메세지 형식: " + message);
        }
    }

    public CreateReservationMessageDTOV1 parseMessage(String message) {
        try {
            return objectMapper.readValue(message, CreateReservationMessageDTOV1.class);
        } catch (Exception e) {
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "유효하지 않은 메세지 형식: " + message);
        }
    }


    // NOTE: 메시지 페이로드 및 컨텐츠 생성
    // NOTE: 대기 순번 알림 발송
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
        return dto.getUser().getUsername() + "님의 대기 순번이 5번째로 다가왔습니다!\\n" +
                "\\n" +
                " 가게 앞에서 대기해주세요.\n" +
                "원격줄서기, 즉시예약\n스마트외식, 큐링!";
    }

    // NOTE: 예약 완료 알림 발송
    public String createReservationPayload(String slackId, CreateReservationMessageDTOV1 dto) throws Exception {

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
                                                dto.getUser().getUsername()
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
                                                dto.getReservation().getRestaurant().getName(),
                                                dto.getReservation().getRestaurant().getTel(),
                                                dto.getReservation().getHeadCount(),
                                                dto.getQueue().getSequence()
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

    public String createMessageContent(CreateReservationMessageDTOV1 dto) {
        return dto.getUser().getUsername() + "님께서는 대기 명단에 정상적으로 접수 되셨습니다.\n" +
                "변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다.\n" +
                "■ 매장명: " + dto.getReservation().getRestaurant().getName() + "\n" +
                "■ 매장 전화번호: " + dto.getReservation().getRestaurant().getTel() + "\n" +
                "■ 인원: " + dto.getReservation().getHeadCount() + "명\n" +
                "■ 대기순번: " + dto.getQueue().getSequence() + "번\n" +
                " 가게 앞에서 대기해주세요.\n" +
                "원격줄서기, 즉시예약\n스마트외식, 큐링!";
    }
}
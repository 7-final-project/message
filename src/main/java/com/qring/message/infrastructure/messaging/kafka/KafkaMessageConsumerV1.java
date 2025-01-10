package com.qring.message.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.message.application.v1.service.SlackServiceV1;
import com.qring.message.domain.model.MessageEntity;
import com.qring.message.domain.repository.MessageRepository;
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

    private final MessageRepository messageRepository;
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
        try {
            String slackEmail = slackServiceV1.extractSlackEmailFromReservationMessage(message);
            String slackId = slackServiceV1.fetchSlackIdByEmail(slackEmail);

            ReservationAndQueueEventDTOV1 dto = parseMessage(message);

            String payload = createPayload(slackId, dto);

            slackServiceV1.sendRequest(slackWebhookUrl, payload);

            MessageEntity messageEntityForSave = MessageEntity.createMessageEntity(
                    dto.getUserId(),
                    createMessageContent(dto)
            );

            messageRepository.save(messageEntityForSave);
        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }

    private String createMessageContent(ReservationAndQueueEventDTOV1 dto) {
        return dto.getUsername() + "님께서는 대기 명단에 정상적으로 접수 되셨습니다.\n" +
                "변동 사항이 발생하신 경우 매장으로 전화주시기 바랍니다.\n" +
                "■ 매장명: " + dto.getRestaurantName() + "\n" +
                "■ 매장 전화번호: " + dto.getRestaurantTel() + "\n" +
                "■ 인원: " + dto.getHeadCount() + "명\n" +
                "■ 대기순번: " + dto.getSequence() + "번\n";
    }

    private String createPayload(String slackId, ReservationAndQueueEventDTOV1 dto) throws Exception {

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

    private ReservationAndQueueEventDTOV1 parseMessage(String message) {
        try {
            return objectMapper.readValue(message, ReservationAndQueueEventDTOV1.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message format: " + message, e);
        }
    }
}

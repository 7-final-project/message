package com.qring.message.infrastructure.messaging.kafka;

import com.qring.message.application.service.MessageServiceV1;
import com.qring.message.application.service.SlackServiceV1;
import com.qring.message.infrastructure.messaging.dto.QueueEventDTOV1;
import com.qring.message.infrastructure.messaging.dto.ReservationAndQueueEventDTOV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "KafkaMessageConsumerV1")
public class KafkaMessageConsumerV1 {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final SlackServiceV1 slackServiceV1;
    private final MessageServiceV1 messageServiceV1;

    /**
     * 대기 5번째 순번인 고객에게 메시지 전송
     */
    @KafkaListener(topics = "${spring.kafka.consumer.topic.userinfo-send-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void sendMessageToUser(String message) {
        try {
            QueueEventDTOV1 dto = slackServiceV1.parseQueueMessage(message);

            String slackId = slackServiceV1.extractSlackIdByEmail(dto.getSlackEmail()); // 슬랙 이메일로 Slack ID 조회

            // 메시지 페이로드 생성
            String content = slackServiceV1.createQueuePayload(slackId, dto.getUsername());

            // 웹훅 URL로 메시지 전송
            slackServiceV1.sendRequest(slackWebhookUrl, content);

            // 메세지 DB에 저장
            messageServiceV1.postBy(dto.getUserId(), slackServiceV1.createQueueMessageContent(dto));

            log.info("Message sent successfully to user with email: {}", dto.getSlackEmail());
        } catch (Exception e) {
            log.error("Error occurred while sending message to user: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${spring.kafka.consumer.topic.queue-reservation-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void sendReservationMessageToUser(String message) {
        try {
            ReservationAndQueueEventDTOV1 dto = slackServiceV1.parseMessage(message);

            String slackId = slackServiceV1.extractSlackIdByEmail(dto.getSlackEmail());

            String content = slackServiceV1.createReservationPayload(slackId, dto);

            slackServiceV1.sendRequest(slackWebhookUrl, content);

            messageServiceV1.postBy(dto.getUserId(), slackServiceV1.createMessageContent(dto));

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}

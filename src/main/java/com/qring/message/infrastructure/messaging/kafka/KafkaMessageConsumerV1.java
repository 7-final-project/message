package com.qring.message.infrastructure.messaging.kafka;

import com.qring.message.application.global.exception.ErrorCode;
import com.qring.message.application.global.exception.MessageException;
import com.qring.message.application.v1.service.MessageServiceV1;
import com.qring.message.application.v1.service.SlackServiceV1;
import com.qring.message.infrastructure.messaging.dto.QueueEventDTOV1;
import com.qring.message.infrastructure.messaging.dto.CreateReservationMessageDTOV1;
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
    private final MessageServiceV1 messageServiceV1;

    // NOTE: 대기 5번째 순번인 고객에게 메시지 전송
    @KafkaListener(topics = "${spring.kafka.consumer.topic.userinfo-send-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void sendMessageToUser(String message) {
        try {
            QueueEventDTOV1 dto = slackServiceV1.parseQueueMessage(message);

            String slackId = slackServiceV1.extractSlackIdByEmail(dto.getUser().getSlackEmail()); // 슬랙 이메일로 Slack ID 조회

            // NOTE: 메시지 페이로드 생성
            String content = slackServiceV1.createQueuePayload(slackId, dto.getUser().getUsername());

            // NOTE: 웹훅 URL로 메시지 전송
            slackServiceV1.sendRequest(slackWebhookUrl, content);

            // NOTE: 메세지 DB에 저장
            messageServiceV1.postBy(dto.getUser().getId(), slackServiceV1.createQueueMessageContent(dto));

            log.info("메세지 발송 성공: {}", dto.getUser().getSlackEmail());
        } catch (Exception e) {
            log.error("메세지 발송 실패: {}", e.getMessage(), e);
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "대기 5번째 고객에게 메세지 발송을 실패했습니다.");
        }
    }

    @KafkaListener(topics = "${spring.kafka.consumer.topic.queue-reservation-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void sendReservationMessageToUser(String message) {
        try {
            CreateReservationMessageDTOV1 dto = slackServiceV1.parseMessage(message);

            String slackId = slackServiceV1.extractSlackIdByEmail(dto.getUser().getSlackEmail());

            String content = slackServiceV1.createReservationPayload(slackId, dto);

            slackServiceV1.sendRequest(slackWebhookUrl, content);

            messageServiceV1.postBy(dto.getUser().getUserId(), slackServiceV1.createMessageContent(dto));

        } catch (Exception e) {
            log.error("메세지 발송 실패: {}", message, e);
            throw new MessageException(ErrorCode.BAD_REQUEST_ERROR, "예약 완료 메세지 발송에 실패했습니다.");
        }
    }
}

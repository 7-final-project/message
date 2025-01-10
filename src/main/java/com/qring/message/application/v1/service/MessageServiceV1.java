package com.qring.message.application.v1.service;

import com.qring.message.application.global.exception.UnauthorizedAccessException;
import com.qring.message.application.v1.res.MessageSearchResDTOV1;
import com.qring.message.domain.model.MessageEntity;
import com.qring.message.domain.repository.MessageRepository;
import com.qring.message.infrastructure.util.PassportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MessageServiceV1 Log")
public class MessageServiceV1 {

    private final MessageRepository messageRepository;

    public void postBy(Long userId, String content) {

        MessageEntity messageEntity = MessageEntity.createMessageEntity(userId, content);

        messageRepository.save(messageEntity);
    }

    public MessageSearchResDTOV1 searchBy(String passport, Pageable pageable, Long userId, Long id, String sort) {

        String userRole = validateUserRole(PassportUtil.getRole(passport));

        Page<MessageEntity> messageEntityPage = messageRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                userRole,
                userId,
                id,
                sort
        );

        return MessageSearchResDTOV1.of(messageEntityPage);
    }

    private String validateUserRole(String role) {

        if ("관리자".equals(role) || "고객".equals(role)) {
            return role;
        } else {
            throw new UnauthorizedAccessException("유효하지 않은 사용자 역할입니다.");
        }
    }
}

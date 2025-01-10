package com.qring.message.domain.repository;

import com.qring.message.domain.model.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepository {

    MessageEntity save(MessageEntity messageEntity);

    Page<MessageEntity> findReservationPageByDeletedAtIsNullWithConditions (Pageable pageable, String userRole, Long userId, Long id, String sort);
}

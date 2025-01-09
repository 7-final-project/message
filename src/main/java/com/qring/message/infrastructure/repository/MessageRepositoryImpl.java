package com.qring.message.infrastructure.repository;

import com.qring.message.domain.model.MessageEntity;
import com.qring.message.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public MessageEntity save(MessageEntity messageEntity) {
        return jpaReservationRepository.save(messageEntity);
    }
}

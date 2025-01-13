package com.qring.message.infrastructure.repository;

import com.qring.message.domain.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationRepository extends JpaRepository<MessageEntity, Long> {
}

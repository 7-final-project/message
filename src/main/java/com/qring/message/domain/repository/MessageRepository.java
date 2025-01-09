package com.qring.message.domain.repository;

import com.qring.message.domain.model.MessageEntity;

public interface MessageRepository {

    MessageEntity save(MessageEntity messageEntity);

}

package com.qring.message.infrastructure.repository;

import com.qring.message.application.global.exception.BadRequestException;
import com.qring.message.domain.model.MessageEntity;
import com.qring.message.domain.model.QMessageEntity;
import com.qring.message.domain.repository.MessageRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaReservationRepository jpaReservationRepository;
    private final JPAQueryFactory jpaQueryFactory;

    public MessageEntity save(MessageEntity messageEntity) {
        return jpaReservationRepository.save(messageEntity);
    }

    // QueryDSL 동적 쿼리
    public Page<MessageEntity> findReservationPageByDeletedAtIsNullWithConditions(Pageable pageable, String userRole, Long messageUserId, Long userId, Long id, String sort) {

        QMessageEntity messageEntity = QMessageEntity.messageEntity;

        // 권한 기반 조건 생성
        BooleanExpression roleCondition = getRoleCondition(userRole, userId);

        // Query 실행
        List<MessageEntity> results = jpaQueryFactory
                .selectFrom(messageEntity)
                .where(
                        roleCondition,
                        messageEntity.deletedAt.isNull(),
                        idEq(id, messageEntity),
                        userIdEq(messageUserId, messageEntity)
                )
                .orderBy(getOrderSpecifier(sort, messageEntity))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPQLQuery<Long> countQuery = jpaQueryFactory
                .select(messageEntity.count())
                .from(messageEntity)
                .where(
                        roleCondition,
                        messageEntity.deletedAt.isNull(),
                        idEq(id, messageEntity),
                        userIdEq(messageUserId, messageEntity)
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // 권한별 조건 메서드
    private BooleanExpression getRoleCondition(String userRole, Long userId) {
        QMessageEntity message = QMessageEntity.messageEntity;

        switch (userRole) {
            case "관리자":
                return Expressions.asBoolean(true).isTrue(); // 관리자는 모든 데이터를 조회 가능
            case "고객":
                return message.userId.eq(userId);
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + userRole);
        }
    }

    // 동적 쿼리 조건 메서드
    private BooleanExpression idEq(Long id, QMessageEntity messageEntity) {
        return id != null ? messageEntity.id.eq(id) : null;
    }

    private BooleanExpression userIdEq(Long messageUserId, QMessageEntity messageEntity) {
        return messageUserId != null ? messageEntity.userId.eq(messageUserId) : null;
    }

    // 정렬 로직
    private OrderSpecifier<?> getOrderSpecifier(String sort, QMessageEntity messageEntity) {
        if (sort == null) {
            return messageEntity.createdAt.desc();
        }

        if (sort.equals("OLDEST")) {
            return messageEntity.createdAt.asc(); // 생성일 오름차순
        }
        return messageEntity.createdAt.desc();
    }
}

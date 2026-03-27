package zoonza.commerce.order.adapter.out.persistence

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import zoonza.commerce.catalog.ProductOptionSnapshot
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.adapter.out.persistence.QOrderItemJpaEntity.Companion.orderItemJpaEntity
import zoonza.commerce.order.adapter.out.persistence.QOrderJpaEntity.Companion.orderJpaEntity
import zoonza.commerce.order.domain.OrderItemStatus

class OrderQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : OrderQueryRepository {
    override fun findReviewablePurchases(
        memberId: Long,
        productId: Long,
        status: OrderItemStatus,
    ): List<ReviewablePurchase> {
        return queryFactory
            .select(orderItemJpaEntity.id, orderItemJpaEntity.optionColorSnapshot, orderItemJpaEntity.optionSizeSnapshot)
            .from(orderJpaEntity)
            .join(orderJpaEntity.items, orderItemJpaEntity)
            .where(
                orderJpaEntity.memberId.eq(memberId),
                orderJpaEntity.deletedAt.isNull(),
                orderItemJpaEntity.productId.eq(productId),
                orderItemJpaEntity.status.eq(status),
            )
            .orderBy(orderItemJpaEntity.confirmedAt.desc(), orderJpaEntity.id.desc(), orderItemJpaEntity.id.desc())
            .fetch()
            .map { tuple ->
                ReviewablePurchase(
                    orderItemId = tuple.get(orderItemJpaEntity.id) ?: throw IllegalStateException("주문상품 식별자를 찾을 수 없습니다."),
                    option = ProductOptionSnapshot(
                        color = tuple.get(orderItemJpaEntity.optionColorSnapshot)
                            ?: throw IllegalStateException("주문상품 옵션 색상을 찾을 수 없습니다."),
                        size = tuple.get(orderItemJpaEntity.optionSizeSnapshot)
                            ?: throw IllegalStateException("주문상품 옵션 사이즈를 찾을 수 없습니다."),
                    ),
                )
            }
    }

    override fun findOrderByMemberIdAndOrderItemId(
        memberId: Long,
        orderItemId: Long,
    ): OrderJpaEntity? {
        return fetchSingleOrder(
            orderJpaEntity.memberId.eq(memberId)
                .and(orderJpaEntity.deletedAt.isNull())
                .and(orderItemJpaEntity.id.eq(orderItemId)),
        )
    }

    override fun findOrderDetailByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): OrderJpaEntity? {
        return fetchSingleOrder(
            orderJpaEntity.id.eq(orderId)
                .and(orderJpaEntity.memberId.eq(memberId))
                .and(orderJpaEntity.deletedAt.isNull()),
            leftJoinItems = true,
        )
    }

    override fun findOrderDetailById(orderId: Long): OrderJpaEntity? {
        return fetchSingleOrder(
            orderJpaEntity.id.eq(orderId)
                .and(orderJpaEntity.deletedAt.isNull()),
            leftJoinItems = true,
        )
    }

    private fun fetchSingleOrder(
        predicate: BooleanExpression,
        leftJoinItems: Boolean = false,
    ): OrderJpaEntity? {
        val query =
            queryFactory
                .selectFrom(orderJpaEntity)
                .distinct()

        if (leftJoinItems) {
            query.leftJoin(orderJpaEntity.items, orderItemJpaEntity).fetchJoin()
        } else {
            query.join(orderJpaEntity.items, orderItemJpaEntity).fetchJoin()
        }

        return query
            .where(predicate)
            .orderBy(orderItemJpaEntity.id.asc())
            .fetch()
            .firstOrNull()
    }
}

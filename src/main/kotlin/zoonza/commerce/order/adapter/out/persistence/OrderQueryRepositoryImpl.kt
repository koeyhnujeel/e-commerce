package zoonza.commerce.order.adapter.out.persistence

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import zoonza.commerce.catalog.ProductOptionSnapshot
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.order.domain.QOrder.Companion.order
import zoonza.commerce.order.domain.QOrderItem.Companion.orderItem

class OrderQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : OrderQueryRepository {
    override fun findReviewablePurchases(
        memberId: Long,
        productId: Long,
        status: OrderItemStatus,
    ): List<ReviewablePurchase> {
        return queryFactory
            .select(orderItem.id, orderItem.optionColorSnapshot, orderItem.optionSizeSnapshot)
            .from(order)
            .join(order.items, orderItem)
            .where(
                order.memberId.eq(memberId),
                order.deletedAt.isNull(),
                orderItem.productId.eq(productId),
                orderItem.status.eq(status),
            )
            .orderBy(orderItem.confirmedAt.desc(), order.id.desc(), orderItem.id.desc())
            .fetch()
            .map { tuple ->
                ReviewablePurchase(
                    orderItemId = tuple.get(orderItem.id) ?: throw IllegalStateException("주문상품 식별자를 찾을 수 없습니다."),
                    option = ProductOptionSnapshot(
                        color = tuple.get(orderItem.optionColorSnapshot)
                            ?: throw IllegalStateException("주문상품 옵션 색상을 찾을 수 없습니다."),
                        size = tuple.get(orderItem.optionSizeSnapshot)
                            ?: throw IllegalStateException("주문상품 옵션 사이즈를 찾을 수 없습니다."),
                    ),
                )
            }
    }

    override fun findOrderByMemberIdAndOrderItemId(
        memberId: Long,
        orderItemId: Long,
    ): Order? {
        return fetchSingleOrder(
            order.memberId.eq(memberId)
                .and(order.deletedAt.isNull())
                .and(orderItem.id.eq(orderItemId)),
        )
    }

    override fun findOrderDetailByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): Order? {
        return fetchSingleOrder(
            order.id.eq(orderId)
                .and(order.memberId.eq(memberId))
                .and(order.deletedAt.isNull()),
            leftJoinItems = true,
        )
    }

    override fun findOrderDetailById(orderId: Long): Order? {
        return fetchSingleOrder(
            order.id.eq(orderId)
                .and(order.deletedAt.isNull()),
            leftJoinItems = true,
        )
    }

    private fun fetchSingleOrder(
        predicate: BooleanExpression,
        leftJoinItems: Boolean = false,
    ): Order? {
        val query =
            queryFactory
                .selectFrom(order)
                .distinct()

        if (leftJoinItems) {
            query.leftJoin(order.items, orderItem).fetchJoin()
        } else {
            query.join(order.items, orderItem).fetchJoin()
        }

        return query
            .where(predicate)
            .orderBy(orderItem.id.asc())
            .fetch()
            .firstOrNull()
    }
}

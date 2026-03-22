package zoonza.commerce.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItemStatus

interface OrderJpaRepository : JpaRepository<Order, Long> {
    @Query(
        """
        select new zoonza.commerce.order.ReviewablePurchase(
            item.id,
            item.optionColorSnapshot,
            item.optionSizeSnapshot
        )
        from Order o
        join o.items item
        where o.memberId = :memberId
          and o.deletedAt is null
          and item.productId = :productId
          and item.status = :status
        order by item.confirmedAt desc, o.id desc, item.id desc
        """,
    )
    fun findReviewablePurchases(
        @Param("memberId") memberId: Long,
        @Param("productId") productId: Long,
        @Param("status") status: OrderItemStatus,
    ): List<ReviewablePurchase>

    @Query(
        """
        select distinct o
        from Order o
        join fetch o.items item
        where o.memberId = :memberId
          and o.deletedAt is null
          and item.id = :orderItemId
        """,
    )
    fun findOrderByMemberIdAndOrderItemId(
        @Param("memberId") memberId: Long,
        @Param("orderItemId") orderItemId: Long,
    ): Order?

    fun findAllByMemberIdAndDeletedAtIsNullOrderByOrderedAtDescIdDesc(memberId: Long): List<Order>

    @Query(
        """
        select distinct o
        from Order o
        left join fetch o.items item
        where o.id = :orderId
          and o.memberId = :memberId
          and o.deletedAt is null
        order by item.id asc
        """,
    )
    fun findOrderDetailByIdAndMemberId(
        @Param("orderId") orderId: Long,
        @Param("memberId") memberId: Long,
    ): Order?

    @Query(
        """
        select distinct o
        from Order o
        left join fetch o.items item
        where o.id = :orderId
          and o.deletedAt is null
        order by item.id asc
        """,
    )
    fun findOrderDetailById(
        @Param("orderId") orderId: Long,
    ): Order?
}

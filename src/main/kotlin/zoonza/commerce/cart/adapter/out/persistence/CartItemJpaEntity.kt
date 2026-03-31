package zoonza.commerce.cart.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.cart.domain.CartItem
import java.time.LocalDateTime

@Entity
@Table(
    name = "cart_item",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_cart_item_cart_id_product_option_id",
            columnNames = ["cart_id", "product_option_id"],
        ),
    ],
)
class CartItemJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: CartJpaEntity? = null,

    @Column(name = "product_id", nullable = false)
    var productId: Long = 0L,

    @Column(name = "product_option_id", nullable = false)
    val productOptionId: Long = 0L,

    @Column(nullable = false)
    var quantity: Long = 0L,

    @Column(name = "added_at", nullable = false)
    var addedAt: LocalDateTime = LocalDateTime.MIN,
) {
    fun toDomain(): CartItem {
        return CartItem(
            id = id,
            productId = productId,
            productOptionId = productOptionId,
            quantity = quantity,
            addedAt = addedAt,
        )
    }

    fun updateFrom(cartItem: CartItem) {
        productId = cartItem.productId
        quantity = cartItem.quantity
        addedAt = cartItem.addedAt
    }

    companion object {
        fun from(
            cartItem: CartItem,
            cart: CartJpaEntity,
        ): CartItemJpaEntity {
            return CartItemJpaEntity(
                id = cartItem.id,
                cart = cart,
                productId = cartItem.productId,
                productOptionId = cartItem.productOptionId,
                quantity = cartItem.quantity,
                addedAt = cartItem.addedAt,
            )
        }
    }
}

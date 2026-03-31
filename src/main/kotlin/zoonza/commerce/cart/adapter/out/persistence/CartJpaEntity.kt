package zoonza.commerce.cart.adapter.out.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import zoonza.commerce.cart.domain.Cart
import java.time.LocalDateTime

@Entity
@Table(
    name = "cart",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_cart_member_id",
            columnNames = ["member_id"],
        ),
    ],
)
class CartJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0L,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.MIN,

    @Version
    @Column(name = "version", nullable = false)
    val version: Long? = null,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("addedAt ASC")
    val items: MutableList<CartItemJpaEntity> = mutableListOf(),
) {
    fun toDomain(): Cart {
        return Cart(
            id = id,
            memberId = memberId,
            items = items.map(CartItemJpaEntity::toDomain).toMutableList(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version,
        )
    }

    fun updateFrom(cart: Cart) {
        memberId = cart.memberId
        createdAt = cart.createdAt
        updatedAt = cart.updatedAt

        val desiredItems = cart.items.associateBy { it.productOptionId }
        items.removeIf { existingItem -> existingItem.productOptionId !in desiredItems.keys }

        val existingItems = items.associateBy { it.productOptionId }
        cart.items.forEach { item ->
            val existingItem = existingItems[item.productOptionId]
            if (existingItem != null) {
                existingItem.updateFrom(item)
            } else {
                items.add(CartItemJpaEntity.from(item, this))
            }
        }
    }

    companion object {
        fun from(cart: Cart): CartJpaEntity {
            val entity =
                CartJpaEntity(
                    id = cart.id,
                    memberId = cart.memberId,
                    createdAt = cart.createdAt,
                    updatedAt = cart.updatedAt,
                    version = cart.version,
                )

            entity.items.addAll(cart.items.map { item -> CartItemJpaEntity.from(item, entity) })
            return entity
        }
    }
}

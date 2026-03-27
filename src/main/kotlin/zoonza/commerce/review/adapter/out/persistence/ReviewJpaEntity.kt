package zoonza.commerce.review.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.review.domain.Review
import java.time.LocalDateTime

@Entity
@Table(
    name = "review",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_review_member_id_product_id",
            columnNames = ["member_id", "product_id"],
        ),
    ],
)
class ReviewJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "product_id", nullable = false)
    val productId: Long = 0,

    @Column(name = "order_item_id", nullable = false)
    val orderItemId: Long = 0,

    @Column(name = "option_color", nullable = false)
    val optionColor: String = "",

    @Column(name = "option_size", nullable = false)
    val optionSize: String = "",

    @Column(nullable = false)
    var rating: Int = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) {
    fun toDomain(): Review {
        return Review(
            id = id,
            memberId = memberId,
            productId = productId,
            orderItemId = orderItemId,
            optionColor = optionColor,
            optionSize = optionSize,
            rating = rating,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
        )
    }

    companion object {
        fun from(review: Review): ReviewJpaEntity {
            return ReviewJpaEntity(
                id = review.id,
                memberId = review.memberId,
                productId = review.productId,
                orderItemId = review.orderItemId,
                optionColor = review.optionColor,
                optionSize = review.optionSize,
                rating = review.rating,
                content = review.content,
                createdAt = review.createdAt,
                updatedAt = review.updatedAt,
                deletedAt = review.deletedAt,
            )
        }
    }
}

package zoonza.commerce.adapter.out.persistence.like

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType
import java.time.LocalDateTime

@Entity
@Table(
    name = "member_like",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_like_member_id_target_type_target_id",
            columnNames = ["member_id", "target_type", "target_id"],
        ),
    ],
)
class LikeJpaEntity private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    val targetType: LikeTargetType,

    @Column(name = "target_id", nullable = false)
    val targetId: Long,

    @Column(name = "liked_at", nullable = false)
    var likedAt: LocalDateTime,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) {
    fun toDomain(): Like {
        return Like.create(
            id = id,
            memberId = memberId,
            targetType = targetType,
            targetId = targetId,
            likedAt = likedAt,
            deletedAt = deletedAt,
        )
    }

    companion object {
        fun from(like: Like): LikeJpaEntity {
            return LikeJpaEntity(
                id = like.id,
                memberId = like.memberId,
                targetType = like.targetType,
                targetId = like.targetId,
                likedAt = like.likedAt,
                deletedAt = like.deletedAt,
            )
        }
    }
}

package zoonza.commerce.like.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import java.time.LocalDateTime

@Entity
@Table(
    name = "member_like",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_like_member_id_target_id_target_type",
            columnNames = ["member_id", "target_id", "target_type"],
        ),
    ],
)
class MemberLikeJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "target_id", nullable = false)
    val targetId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    val likeTargetType: LikeTargetType = LikeTargetType.PRODUCT,

    @Column(name = "liked_at", nullable = false)
    val likedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,
) {
    fun toDomain(): MemberLike {
        return MemberLike(
            id = id,
            memberId = memberId,
            targetId = targetId,
            likeTargetType = likeTargetType,
            likedAt = likedAt,
            deletedAt = deletedAt,
        )
    }

    companion object {
        fun from(memberLike: MemberLike): MemberLikeJpaEntity {
            return MemberLikeJpaEntity(
                id = memberLike.id,
                memberId = memberLike.memberId,
                targetId = memberLike.targetId,
                likeTargetType = memberLike.likeTargetType,
                likedAt = memberLike.likedAt,
                deletedAt = memberLike.deletedAt,
            )
        }
    }
}

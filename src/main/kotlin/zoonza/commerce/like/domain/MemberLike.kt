package zoonza.commerce.like.domain

import jakarta.persistence.*
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
class MemberLike private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "target_id", nullable = false)
    val targetId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    val targetType: LikeTargetType,

    @Column(name = "liked_at", nullable = false)
    var likedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            memberId: Long,
            targetId: Long,
            targetType: LikeTargetType,
        ): MemberLike {
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(targetId > 0) { "좋아요 대상 ID는 1 이상이어야 합니다." }

            return MemberLike(
                memberId = memberId,
                targetId = targetId,
                targetType = targetType,
            )
        }
    }

    fun cancel() {
        this.deletedAt = LocalDateTime.now()
    }

    fun restore() {
        this.likedAt = LocalDateTime.now()
        this.deletedAt = null
    }

    fun isActive(): Boolean {
        return deletedAt == null
    }
}

package zoonza.commerce.adapter.out.persistence.like

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.like.LikeTargetType

interface LikeJpaRepository : JpaRepository<LikeJpaEntity, Long> {
    fun findByMemberIdAndTargetTypeAndTargetId(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ): LikeJpaEntity?

    fun countByMemberIdAndTargetTypeAndTargetId(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ): Long
}

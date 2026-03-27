package zoonza.commerce.like.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.like.domain.LikeTargetType

interface MemberLikeJpaRepository : JpaRepository<MemberLikeJpaEntity, Long> {
    fun findByMemberIdAndTargetIdAndLikeTargetType(
        memberId: Long,
        targetId: Long,
        likeTargetType: LikeTargetType,
    ): MemberLikeJpaEntity?
}

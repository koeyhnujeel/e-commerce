package zoonza.commerce.like.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

interface MemberLikeJpaRepository : JpaRepository<MemberLike, Long>, MemberLikeQueryRepository {
    fun findByMemberIdAndTargetIdAndTargetType(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ): MemberLike?
}

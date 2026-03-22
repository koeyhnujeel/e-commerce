package zoonza.commerce.like.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

interface MemberLikeJpaRepository : JpaRepository<MemberLike, Long> {
    fun findByMemberIdAndTargetIdAndTargetType(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ): MemberLike?

    fun countByMemberIdAndTargetTypeAndTargetId(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ): Long

    @Query(
        """
        select ml.targetId as targetId, count(ml) as likeCount
        from MemberLike ml
        where ml.targetType = :targetType
          and ml.targetId in :targetIds
          and ml.deletedAt is null
        group by ml.targetId
        """,
    )
    fun countActiveByTargetTypeAndTargetIdIn(
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<LikeCountProjection>

    @Query(
        """
        select ml.targetId
        from MemberLike ml
        where ml.memberId = :memberId
          and ml.targetType = :targetType
          and ml.targetId in :targetIds
          and ml.deletedAt is null
        """,
    )
    fun findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        memberId: Long,
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<Long>
}

interface LikeCountProjection {
    val targetId: Long
    val likeCount: Long
}

package zoonza.commerce.like.adapter.out.persistence

import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import zoonza.commerce.like.domain.LikeTargetType

interface MemberLikeJpaRepository : JpaRepository<MemberLikeJpaEntity, Long> {
    fun findByMemberIdAndTargetIdAndLikeTargetType(
        memberId: Long,
        targetId: Long,
        likeTargetType: LikeTargetType,
    ): MemberLikeJpaEntity?

    @Query(
        """
        SELECT ml.targetId
        FROM MemberLikeJpaEntity ml
        WHERE ml.memberId = :memberId
          AND ml.likeTargetType = :likeTargetType
          AND ml.targetId in :targetIds
          AND ml.deletedAt is null
        """,
    )
    fun findActiveTargetIds(
        @Param("memberId") memberId: Long,
        @Param("likeTargetType") likeTargetType: LikeTargetType,
        @Param("targetIds") targetIds: Collection<Long>,
    ): List<Long>
}

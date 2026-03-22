package zoonza.commerce.like.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.QMemberLike.Companion.memberLike

class MemberLikeQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : MemberLikeQueryRepository {
    override fun countActiveByTargetTypeAndTargetIdIn(
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<LikeCountRow> {
        val likeCount = memberLike.id.count()

        return queryFactory
            .select(memberLike.targetId, likeCount)
            .from(memberLike)
            .where(
                memberLike.targetType.eq(targetType),
                memberLike.targetId.`in`(targetIds),
                memberLike.deletedAt.isNull(),
            )
            .groupBy(memberLike.targetId)
            .fetch()
            .map { tuple ->
                LikeCountRow(
                    targetId = tuple.get(memberLike.targetId)!!,
                    likeCount = tuple.get(likeCount) ?: 0L,
                )
            }
    }

    override fun findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        memberId: Long,
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<Long> {
        return queryFactory
            .select(memberLike.targetId)
            .from(memberLike)
            .where(
                memberLike.memberId.eq(memberId),
                memberLike.targetType.eq(targetType),
                memberLike.targetId.`in`(targetIds),
                memberLike.deletedAt.isNull(),
            )
            .fetch()
    }
}

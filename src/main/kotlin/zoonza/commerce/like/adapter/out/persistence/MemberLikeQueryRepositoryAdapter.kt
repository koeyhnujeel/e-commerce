package zoonza.commerce.like.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import zoonza.commerce.like.adapter.out.persistence.QMemberLikeJpaEntity.Companion.memberLikeJpaEntity
import zoonza.commerce.like.application.port.out.MemberLikeQueryRepository
import zoonza.commerce.like.domain.LikeTargetType

@Repository
class MemberLikeQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : MemberLikeQueryRepository {
    override fun findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        memberId: Long,
        likeTargetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<Long> {
        return queryFactory
            .select(memberLikeJpaEntity.targetId)
            .from(memberLikeJpaEntity)
            .where(
                memberLikeJpaEntity.memberId.eq(memberId),
                memberLikeJpaEntity.likeTargetType.eq(likeTargetType),
                memberLikeJpaEntity.targetId.`in`(targetIds),
                memberLikeJpaEntity.deletedAt.isNull(),
            )
            .fetch()
    }
}

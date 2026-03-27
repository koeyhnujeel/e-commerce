package zoonza.commerce.like.domain

interface LikeRepository {
    fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        likeTargetType: LikeTargetType,
    ): MemberLike?

    fun findActiveTargetIds(
        memberId: Long,
        targetIds: Collection<Long>,
        likeTargetType: LikeTargetType,
    ): Set<Long>

    fun save(memberLike: MemberLike): MemberLike
}
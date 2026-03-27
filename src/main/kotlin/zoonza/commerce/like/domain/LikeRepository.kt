package zoonza.commerce.like.domain

interface LikeRepository {
    fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        likeTargetType: LikeTargetType,
    ): MemberLike?

    fun findLikedProduct(
        memberId: Long,
        targetIds: Collection<Long>,
        likeTargetType: LikeTargetType
    ): List<Long>

    fun save(memberLike: MemberLike): MemberLike
}
package zoonza.commerce.like.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.like.LikeApi
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.like.application.port.out.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

@Service
class DefaultLikeService(
    private val likeRepository: LikeRepository,
) : LikeApi, LikeService {
    @Transactional(readOnly = true)
    override fun countProductLikes(productId: Long): Long {
        return countProductLikes(listOf(productId))[productId] ?: 0L
    }

    @Transactional(readOnly = true)
    override fun countProductLikes(productIds: Collection<Long>): Map<Long, Long> {
        return likeRepository.countByTargetIds(productIds, LikeTargetType.PRODUCT)
    }

    @Transactional(readOnly = true)
    override fun findLikedProductIds(
        memberId: Long,
        productIds: Collection<Long>,
    ): Set<Long> {
        return likeRepository.findActiveTargetIds(memberId, productIds, LikeTargetType.PRODUCT)
    }

    @Transactional
    override fun like(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ) {
        val like = likeRepository.findByMemberIdAndTargetId(memberId, targetId, targetType)

        if (like != null) {
            like.restore()
            likeRepository.save(like)
        } else {
            val newMemberLike = MemberLike.create(memberId, targetId, targetType)
            likeRepository.save(newMemberLike)
        }
    }

    @Transactional
    override fun cancelLike(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ) {
        val like = likeRepository.findByMemberIdAndTargetId(memberId, targetId, targetType)
            ?: return

        like.cancel()

        likeRepository.save(like)
    }
}

package zoonza.commerce.like.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.like.application.port.out.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

@Service
class DefaultLikeService(
    private val likeRepository: LikeRepository,
) : LikeService {
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

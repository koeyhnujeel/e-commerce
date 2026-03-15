package zoonza.commerce.like.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType
import zoonza.commerce.like.port.`in`.LikeService
import zoonza.commerce.like.port.out.LikeRepository
import zoonza.commerce.like.port.out.LikeTargetReader
import java.time.LocalDateTime

@Service
class DefaultLikeService(
    private val likeRepository: LikeRepository,
    private val likeTargetReader: LikeTargetReader,
) : LikeService {
    @Transactional
    override fun like(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ) {
        assertTargetExists(targetType, targetId)

        val existingLike = likeRepository.findByMemberIdAndTarget(memberId, targetType, targetId)

        when {
            existingLike == null ->
                likeRepository.saveIfAbsent(
                    Like.create(
                        memberId = memberId,
                        targetType = targetType,
                        targetId = targetId,
                        likedAt = LocalDateTime.now(),
                    ),
                )

            existingLike.isDeleted() -> {
                existingLike.restore(LocalDateTime.now())
                likeRepository.save(existingLike)
            }
        }
    }

    @Transactional
    override fun cancel(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ) {
        assertTargetExists(targetType, targetId)

        val existingLike = likeRepository.findByMemberIdAndTarget(memberId, targetType, targetId) ?: return

        if (existingLike.isDeleted()) {
            return
        }

        existingLike.cancel(LocalDateTime.now())
        likeRepository.save(existingLike)
    }

    private fun assertTargetExists(
        targetType: LikeTargetType,
        targetId: Long,
    ) {
        if (likeTargetReader.exists(targetType, targetId)) {
            return
        }

        val errorCode = resolveNotFoundErrorCode(targetType)

        throw BusinessException(errorCode)
    }

    private fun resolveNotFoundErrorCode(targetType: LikeTargetType): ErrorCode {
        return when (targetType) {
            LikeTargetType.PRODUCT -> ErrorCode.PRODUCT_NOT_FOUND
        }
    }
}

package zoonza.commerce.adapter.out.persistence.like

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType
import zoonza.commerce.like.port.out.LikeRepository

@Repository
class LikeRepositoryAdapter(
    private val likeJpaRepository: LikeJpaRepository,
) : LikeRepository {
    override fun findByMemberIdAndTarget(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ): Like? {
        return likeJpaRepository
            .findByMemberIdAndTargetTypeAndTargetId(memberId, targetType, targetId)
            ?.toDomain()
    }

    override fun save(like: Like): Like {
        return likeJpaRepository.save(LikeJpaEntity.from(like)).toDomain()
    }

    override fun saveIfAbsent(like: Like): Like {
        return try {
            likeJpaRepository.saveAndFlush(LikeJpaEntity.from(like)).toDomain()
        } catch (_: DataIntegrityViolationException) {
            findByMemberIdAndTarget(like.memberId, like.targetType, like.targetId)
                ?: throw IllegalStateException("기존 좋아요를 다시 조회하지 못했습니다.")
        }
    }
}

package zoonza.commerce.adapter.out.persistence.like

import org.springframework.stereotype.Component
import zoonza.commerce.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.like.LikeTargetType
import zoonza.commerce.like.port.out.LikeTargetReader

@Component
class JpaLikeTargetReader(
    private val productJpaRepository: ProductJpaRepository,
) : LikeTargetReader {
    override fun exists(
        targetType: LikeTargetType,
        targetId: Long,
    ): Boolean {
        return when (targetType) {
            LikeTargetType.PRODUCT -> productJpaRepository.existsById(targetId)
        }
    }
}

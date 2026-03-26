package zoonza.commerce.catalog.application.service

import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.like.ProductLikeCanceled
import zoonza.commerce.like.ProductLiked

@Service
class ProductLikeEventHandler(
    private val productStatisticRepository: ProductStatisticRepository,
) {
    @ApplicationModuleListener
    fun handle(event: ProductLiked) {
        productStatisticRepository.applyLikeCountDelta(
            productId = event.productId,
            delta = 1L,
        )
    }

    @ApplicationModuleListener
    fun handle(event: ProductLikeCanceled) {
        productStatisticRepository.applyLikeCountDelta(
            productId = event.productId,
            delta = -1L,
        )
    }
}

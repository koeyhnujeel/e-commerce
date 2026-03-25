package zoonza.commerce.catalog.application.service

import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import zoonza.commerce.catalog.application.port.out.ProductStatisticRepository
import zoonza.commerce.like.ProductLikeCountChanged

@Service
class ProductLikeCountEventHandler(
    private val productStatisticRepository: ProductStatisticRepository,
) {
    @ApplicationModuleListener
    fun handle(event: ProductLikeCountChanged) {
        productStatisticRepository.applyLikeCountDelta(
            productId = event.productId,
            delta = event.delta,
        )
    }
}

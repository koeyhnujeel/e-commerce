package zoonza.commerce.catalog.application.service

import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import zoonza.commerce.like.ProductLiked
import zoonza.commerce.like.ProductUnliked

@Service
class ProductLikeEventHandler(
    private val defaultProductStatisticService: DefaultProductStatisticService
) {
    @ApplicationModuleListener
    fun handle(event: ProductLiked) {
        defaultProductStatisticService.incrementLikeCount(event.productId)
    }

    @ApplicationModuleListener
    fun handle(event: ProductUnliked) {
        defaultProductStatisticService.decrementLikeCount(event.productId)
    }
}

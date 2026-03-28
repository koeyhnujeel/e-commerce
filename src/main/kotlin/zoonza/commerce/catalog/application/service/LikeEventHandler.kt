package zoonza.commerce.catalog.application.service

import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import zoonza.commerce.shared.ProductLiked
import zoonza.commerce.shared.ProductUnliked

@Service
class LikeEventHandler(
    private val defaultProductStatisticService: DefaultProductStatisticService
) {
    @ApplicationModuleListener
    fun handle(event: ProductLiked) {
        defaultProductStatisticService.incrementProductLikeCountWithUpdate(event.productId)
    }

    @ApplicationModuleListener
    fun handle(event: ProductUnliked) {
        defaultProductStatisticService.decrementProductLikeCountWithUpdate(event.productId)
    }
}

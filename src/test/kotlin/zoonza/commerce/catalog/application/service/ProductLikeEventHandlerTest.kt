package zoonza.commerce.catalog.application.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.like.ProductLiked
import zoonza.commerce.like.ProductUnliked

class ProductLikeEventHandlerTest {
    private val productStatisticService = mockk<DefaultProductStatisticService>(relaxed = true)
    private val handler = ProductLikeEventHandler(productStatisticService)

    @Test
    fun `상품 좋아요 이벤트를 받으면 좋아요 수를 증가시킨다`() {
        handler.handle(ProductLiked(productId = 10L))

        verify(exactly = 1) { productStatisticService.incrementLikeCount(10L) }
    }

    @Test
    fun `상품 좋아요 취소 이벤트를 받으면 좋아요 수를 감소시킨다`() {
        handler.handle(ProductUnliked(productId = 10L))

        verify(exactly = 1) { productStatisticService.decrementLikeCount(10L) }
    }
}

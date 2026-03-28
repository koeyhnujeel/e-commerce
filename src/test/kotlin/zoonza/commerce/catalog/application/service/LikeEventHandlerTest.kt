package zoonza.commerce.catalog.application.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.ProductLiked
import zoonza.commerce.shared.ProductUnliked

class LikeEventHandlerTest {
    private val productStatisticService = mockk<DefaultProductStatisticService>(relaxed = true)
    private val handler = LikeEventHandler(productStatisticService)

    @Test
    fun `상품 좋아요 이벤트를 받으면 좋아요 수를 증가시킨다`() {
        handler.handle(ProductLiked(productId = 10L))

        verify(exactly = 1) { productStatisticService.incrementProductLikeCountWithUpdate(10L) }
    }

    @Test
    fun `상품 좋아요 취소 이벤트를 받으면 좋아요 수를 감소시킨다`() {
        handler.handle(ProductUnliked(productId = 10L))

        verify(exactly = 1) { productStatisticService.decrementProductLikeCountWithUpdate(10L) }
    }
}

package zoonza.commerce.catalog.application.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.application.port.out.ProductStatisticRepository
import zoonza.commerce.like.ProductLikeCanceled
import zoonza.commerce.like.ProductLiked

class ProductLikeEventHandlerTest {
    private val productStatisticRepository = mockk<ProductStatisticRepository>(relaxed = true)
    private val handler = ProductLikeEventHandler(productStatisticRepository)

    @Test
    fun `상품 좋아요 이벤트를 받으면 좋아요 수를 증가시킨다`() {
        handler.handle(ProductLiked(productId = 10L))

        verify(exactly = 1) {
            productStatisticRepository.applyLikeCountDelta(
                productId = 10L,
                delta = 1L,
            )
        }
    }

    @Test
    fun `상품 좋아요 취소 이벤트를 받으면 좋아요 수를 감소시킨다`() {
        handler.handle(ProductLikeCanceled(productId = 10L))

        verify(exactly = 1) {
            productStatisticRepository.applyLikeCountDelta(
                productId = 10L,
                delta = -1L,
            )
        }
    }
}

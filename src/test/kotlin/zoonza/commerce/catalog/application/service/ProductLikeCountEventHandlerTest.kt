package zoonza.commerce.catalog.application.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.application.port.out.ProductStatisticRepository
import zoonza.commerce.like.ProductLikeCountChanged

class ProductLikeCountEventHandlerTest {
    private val productStatisticRepository = mockk<ProductStatisticRepository>(relaxed = true)
    private val handler = ProductLikeCountEventHandler(productStatisticRepository)

    @Test
    fun `좋아요 수 변경 이벤트를 받으면 통계 저장소에 delta를 반영한다`() {
        handler.handle(ProductLikeCountChanged(productId = 10L, delta = 1L))

        verify(exactly = 1) {
            productStatisticRepository.applyLikeCountDelta(
                productId = 10L,
                delta = 1L,
            )
        }
    }
}

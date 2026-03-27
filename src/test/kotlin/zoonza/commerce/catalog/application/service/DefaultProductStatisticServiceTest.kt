package zoonza.commerce.catalog.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.CatalogErrorCode
import zoonza.commerce.catalog.domain.product.ProductRepository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.shared.BusinessException

class DefaultProductStatisticServiceTest {
    private val productRepository = mockk<ProductRepository>()
    private val productStatisticRepository = mockk<ProductStatisticRepository>()
    private val productStatisticService =
        DefaultProductStatisticService(
            productRepository = productRepository,
            productStatisticRepository = productStatisticRepository,
        )

    @Test
    fun `상품이 존재하면 좋아요 수를 증가시킨다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productRepository.existsById(10L) } returns true
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } returns statistic

        productStatisticService.incrementProductLikeCount(10L)

        statistic.likeCount shouldBe 3L
        verify(exactly = 1) { productRepository.existsById(10L) }
        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }

    @Test
    fun `상품이 존재하면 좋아요 수를 감소시킨다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productRepository.existsById(10L) } returns true
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } returns statistic

        productStatisticService.decrementProductLikeCount(10L)

        statistic.likeCount shouldBe 1L
        verify(exactly = 1) { productRepository.existsById(10L) }
        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }

    @Test
    fun `상품이 없으면 좋아요 수 증가는 예외를 던진다`() {
        every { productRepository.existsById(10L) } returns false

        val exception = shouldThrow<BusinessException> {
            productStatisticService.incrementProductLikeCount(10L)
        }

        exception.errorCode shouldBe CatalogErrorCode.PRODUCT_NOT_FOUND
        verify(exactly = 1) { productRepository.existsById(10L) }
        verify(exactly = 0) { productStatisticRepository.findByProductId(any()) }
        verify(exactly = 0) { productStatisticRepository.save(any()) }
    }
}

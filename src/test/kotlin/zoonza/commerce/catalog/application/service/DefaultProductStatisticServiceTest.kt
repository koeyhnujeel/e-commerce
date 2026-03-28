package zoonza.commerce.catalog.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.orm.ObjectOptimisticLockingFailureException
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticErrorCode
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.shared.BusinessException

class DefaultProductStatisticServiceTest {
    private val productStatisticRepository = mockk<ProductStatisticRepository>()
    private val productStatisticService =
        DefaultProductStatisticService(
            productStatisticRepository = productStatisticRepository,
        )

    @Test
    fun `상품이 존재하면 좋아요 수를 증가시킨다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } returns statistic

        productStatisticService.incrementProductLikeCount(10L)

        statistic.likeCount shouldBe 3L
        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }

    @Test
    fun `상품이 존재하면 좋아요 수를 감소시킨다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } returns statistic

        productStatisticService.decrementProductLikeCount(10L)

        statistic.likeCount shouldBe 1L
        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }

    @Test
    fun `상품 통계 정보가 없으면 좋아요 수 증가는 예외를 던진다`() {
        every { productStatisticRepository.findByProductId(10L) } returns null

        val exception = shouldThrow<BusinessException> {
            productStatisticService.incrementProductLikeCount(10L)
        }

        exception.errorCode shouldBe ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND
        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 0) { productStatisticRepository.save(any()) }
    }

    @Test
    fun `update 문으로 상품이 존재하면 좋아요 수를 증가시킨다`() {
        every { productStatisticRepository.incrementLikeCount(10L) } returns 1

        productStatisticService.incrementProductLikeCountWithUpdate(10L)

        verify(exactly = 1) { productStatisticRepository.incrementLikeCount(10L) }
        verify(exactly = 0) { productStatisticRepository.findByProductId(any()) }
    }

    @Test
    fun `update 문으로 상품 통계 정보가 없으면 좋아요 수 증가는 예외를 던진다`() {
        every { productStatisticRepository.incrementLikeCount(10L) } returns 0

        val exception = shouldThrow<BusinessException> {
            productStatisticService.incrementProductLikeCountWithUpdate(10L)
        }

        exception.errorCode shouldBe ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND
        verify(exactly = 1) { productStatisticRepository.incrementLikeCount(10L) }
    }

    @Test
    fun `update 문으로 상품이 존재하면 좋아요 수를 감소시킨다`() {
        every { productStatisticRepository.decrementLikeCount(10L) } returns 1

        productStatisticService.decrementProductLikeCountWithUpdate(10L)

        verify(exactly = 1) { productStatisticRepository.decrementLikeCount(10L) }
        verify(exactly = 0) { productStatisticRepository.findByProductId(any()) }
    }

    @Test
    fun `update 문으로 상품 통계 정보가 없으면 좋아요 수 감소는 예외를 던진다`() {
        every { productStatisticRepository.decrementLikeCount(10L) } returns 0

        val exception = shouldThrow<BusinessException> {
            productStatisticService.decrementProductLikeCountWithUpdate(10L)
        }

        exception.errorCode shouldBe ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND
        verify(exactly = 1) { productStatisticRepository.decrementLikeCount(10L) }
    }

    @Test
    fun `optimistic lock로 상품이 존재하면 좋아요 수를 증가시킨다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } returns statistic

        productStatisticService.incrementProductLikeCount(10L)

        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }

    @Test
    fun `optimistic lock로 상품이 존재하면 좋아요 수를 감소시킨다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } returns statistic

        productStatisticService.decrementProductLikeCount(10L)

        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }

    @Test
    fun `optimistic lock로 상품 통계 정보가 없으면 좋아요 수 증가는 예외를 던진다`() {
        every { productStatisticRepository.findByProductId(10L) } returns null

        val exception = shouldThrow<BusinessException> {
            productStatisticService.incrementProductLikeCount(10L)
        }

        exception.errorCode shouldBe ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND
        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
    }

    @Test
    fun `optimistic lock로 좋아요 수가 0이면 감소는 예외를 던진다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 0L)
        every { productStatisticRepository.findByProductId(10L) } returns statistic

        shouldThrow<IllegalStateException> {
            productStatisticService.decrementProductLikeCount(10L)
        }

        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
    }

    @Test
    fun `optimistic lock 충돌이 나면 일반 증가 메소드도 예외를 전파한다`() {
        val statistic = ProductStatistic.create(productId = 10L, likeCount = 2L)
        every { productStatisticRepository.findByProductId(10L) } returns statistic
        every { productStatisticRepository.save(statistic) } throws ObjectOptimisticLockingFailureException(ProductStatistic::class.java, 10L)

        shouldThrow<ObjectOptimisticLockingFailureException> {
            productStatisticService.incrementProductLikeCount(10L)
        }

        verify(exactly = 1) { productStatisticRepository.findByProductId(10L) }
        verify(exactly = 1) { productStatisticRepository.save(statistic) }
    }
}

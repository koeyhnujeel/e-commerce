package zoonza.commerce.catalog.adapter.out.persistence

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.statistic.ProductStatisticJpaRepository
import zoonza.commerce.catalog.adapter.out.persistence.statistic.ProductStatisticRepositoryAdapter
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.persistence.QuerydslConfig

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class, QuerydslConfig::class, ProductStatisticRepositoryAdapter::class)
class ProductStatisticRepositoryAdapterTest {
    @Autowired
    private lateinit var productStatisticRepository: ProductStatisticRepository

    @Autowired
    private lateinit var productStatisticJpaRepository: ProductStatisticJpaRepository

    @Test
    fun `기존 통계 row가 있으면 좋아요 수를 원자적으로 증가시킨다`() {
        productStatisticJpaRepository.save(
            ProductStatistic.create(
                productId = 10L,
                likeCount = 3L,
            ),
        )

        productStatisticRepository.applyLikeCountDelta(
            productId = 10L,
            delta = 2L,
        )

        productStatisticRepository.findLikeCount(10L) shouldBe 5L
    }

    @Test
    fun `기존 통계 row가 있으면 좋아요 수는 0 아래로 내려가지 않는다`() {
        productStatisticJpaRepository.save(
            ProductStatistic.create(
                productId = 10L,
                likeCount = 1L,
            ),
        )

        productStatisticRepository.applyLikeCountDelta(
            productId = 10L,
            delta = -2L,
        )

        productStatisticRepository.findLikeCount(10L) shouldBe 0L
    }

    @Test
    fun `통계 row가 없고 양수 delta면 새 row를 생성한다`() {
        productStatisticRepository.applyLikeCountDelta(
            productId = 10L,
            delta = 2L,
        )

        productStatisticRepository.findLikeCount(10L) shouldBe 2L
    }

    @Test
    fun `통계 row가 없고 음수 delta면 아무 일도 하지 않는다`() {
        productStatisticRepository.applyLikeCountDelta(
            productId = 10L,
            delta = -1L,
        )

        productStatisticRepository.findLikeCount(10L) shouldBe 0L
    }
}

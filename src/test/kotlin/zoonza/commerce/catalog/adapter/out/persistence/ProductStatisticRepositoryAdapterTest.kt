package zoonza.commerce.catalog.adapter.out.persistence

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.statistic.ProductStatisticJpaEntity
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
    fun `상품 통계를 저장하고 조회한다`() {
        productStatisticRepository.save(
            ProductStatistic.create(
                productId = 10L,
                likeCount = 3L,
            ),
        )

        val found = productStatisticRepository.findByProductId(10L)

        found.shouldNotBeNull()
        found.productId shouldBe 10L
        found.likeCount shouldBe 3L
    }

    @Test
    fun `기존 상품 통계를 수정 후 저장하면 갱신된다`() {
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = 10L,
                    likeCount = 1L,
                ),
            ),
        )
        val statistic = productStatisticRepository.findByProductId(10L).shouldNotBeNull()

        statistic.incrementLikeCount()
        statistic.incrementLikeCount()
        productStatisticRepository.save(statistic)

        val updated = productStatisticRepository.findByProductId(10L)

        updated.shouldNotBeNull()
        updated.likeCount shouldBe 3L
    }
}

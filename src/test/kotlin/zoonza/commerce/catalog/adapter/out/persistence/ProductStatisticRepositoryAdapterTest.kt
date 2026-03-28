package zoonza.commerce.catalog.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
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
import zoonza.commerce.catalog.domain.statistic.ProductStatisticErrorCode
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.shared.BusinessException
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
        val saved = productStatisticRepository.save(
            ProductStatistic.create(
                productId = 10L,
                likeCount = 3L,
            ),
        )

        val found = productStatisticRepository.findByProductId(10L)

        found.shouldNotBeNull()
        saved.id shouldBe found.id
        (found.id > 0L) shouldBe true
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
        updated.id shouldBe statistic.id
        updated.likeCount shouldBe 3L
    }

    @Test
    fun `기존 상품 통계를 수정 후 저장하면 버전도 증가한다`() {
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = 10L,
                    likeCount = 1L,
                ),
            ),
        )
        val beforeVersion = productStatisticJpaRepository.findByProductId(10L).shouldNotBeNull().version
        val statistic = productStatisticRepository.findByProductId(10L).shouldNotBeNull()

        statistic.incrementLikeCount()
        productStatisticRepository.save(statistic)

        val updated = productStatisticJpaRepository.findByProductId(10L).shouldNotBeNull()

        updated.likeCount shouldBe 2L
        updated.version shouldBe beforeVersion?.plus(1)
    }

    @Test
    fun `직접 update 문으로 좋아요 수를 증가해도 버전은 유지된다`() {
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = 10L,
                    likeCount = 1L,
                ),
            ),
        )
        val beforeVersion = productStatisticJpaRepository.findByProductId(10L).shouldNotBeNull().version

        val updatedRowCount = productStatisticRepository.incrementLikeCount(10L)
        val updated = productStatisticJpaRepository.findByProductId(10L).shouldNotBeNull()

        updatedRowCount shouldBe 1
        updated.likeCount shouldBe 2L
        updated.version shouldBe beforeVersion
    }

    @Test
    fun `존재하지 않는 id로 저장을 시도하면 예외를 던진다`() {
        val exception = shouldThrow<BusinessException> {
            productStatisticRepository.save(
                ProductStatistic.create(
                    id = 999L,
                    productId = 10L,
                    likeCount = 1L,
                ),
            )
        }

        exception.errorCode shouldBe ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND
    }
}

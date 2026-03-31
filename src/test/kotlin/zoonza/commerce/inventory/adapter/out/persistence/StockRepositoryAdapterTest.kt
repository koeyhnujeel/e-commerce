package zoonza.commerce.inventory.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.inventory.domain.StockRepository
import zoonza.commerce.inventory.domain.StockReservationStatus
import zoonza.commerce.support.MySqlTestContainerConfig
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class StockRepositoryAdapterTest {
    @Autowired
    private lateinit var stockRepository: StockRepository

    @Autowired
    private lateinit var stockJpaRepository: StockJpaRepository

    @Test
    fun `재고 aggregate를 저장하고 조회한다`() {
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        val stock =
            Stock.create(productOptionId = 101L, totalQuantity = 10L).apply {
                reserve("ORDER-001", 2L, reservedAt, reservedAt.plusMinutes(10))
            }

        val saved = stockRepository.save(stock)
        val found = stockRepository.findByProductOptionId(101L)

        saved.id shouldBe found?.id
        found?.totalQuantity shouldBe 10L
        found?.reservedQuantity shouldBe 2L
        found?.reservations?.single()?.orderNumber shouldBe "ORDER-001"
        found?.reservations?.single()?.status shouldBe StockReservationStatus.RESERVED
    }

    @Test
    fun `같은 상품 옵션으로 재고를 두 번 저장할 수 없다`() {
        stockJpaRepository.save(StockJpaEntity.from(Stock.create(productOptionId = 101L, totalQuantity = 10L)))
        stockJpaRepository.flush()

        shouldThrow<DataIntegrityViolationException> {
            stockJpaRepository.saveAndFlush(StockJpaEntity.from(Stock.create(productOptionId = 101L, totalQuantity = 5L)))
        }
    }
}

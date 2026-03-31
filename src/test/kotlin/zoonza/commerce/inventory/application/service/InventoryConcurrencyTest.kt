package zoonza.commerce.inventory.application.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaRepository
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.support.MySqlTestContainerConfig
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig::class)
class InventoryConcurrencyTest {
    @Autowired
    private lateinit var inventoryApi: InventoryApi

    @Autowired
    private lateinit var stockJpaRepository: StockJpaRepository

    @BeforeEach
    fun setUp() {
        stockJpaRepository.deleteAll()
        stockJpaRepository.flush()
    }

    @Test
    fun `동시에 하나만 예약 가능한 재고를 예약하면 한 요청만 성공한다`() {
        val productOptionId = 501L
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        inventoryApi.createStock(productOptionId = productOptionId, totalQuantity = 1L)

        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        runConcurrently(2) { index ->
            try {
                inventoryApi.reserve(
                    productOptionId = productOptionId,
                    orderNumber = "ORDER-00$index",
                    quantity = 1L,
                    reservedAt = reservedAt,
                    expiresAt = reservedAt.plusMinutes(10),
                )
                successCount.incrementAndGet()
            } catch (_: BusinessException) {
                failureCount.incrementAndGet()
            } catch (_: OptimisticLockingFailureException) {
                failureCount.incrementAndGet()
            } catch (_: CannotAcquireLockException) {
                failureCount.incrementAndGet()
            }
        }

        val stock = inventoryApi.getStock(productOptionId)

        successCount.get() shouldBe 1
        failureCount.get() shouldBe 1
        stock.totalQuantity shouldBe 1L
        stock.reservedQuantity shouldBe 1L
        stock.availableQuantity shouldBe 0L
    }

    private fun runConcurrently(
        threadCount: Int,
        action: (index: Int) -> Unit,
    ) {
        val executor = Executors.newFixedThreadPool(threadCount)
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)

        val futures =
            (1..threadCount).map { index ->
                executor.submit<Unit> {
                    readyLatch.countDown()
                    startLatch.await()
                    try {
                        action(index)
                    } finally {
                        doneLatch.countDown()
                    }
                }
            }

        readyLatch.await()
        startLatch.countDown()
        doneLatch.await()
        futures.forEach { it.get() }
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }
}

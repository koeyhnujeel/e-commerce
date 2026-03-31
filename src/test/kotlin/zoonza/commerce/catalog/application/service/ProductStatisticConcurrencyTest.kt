package zoonza.commerce.catalog.application.service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.context.ActiveProfiles
import zoonza.commerce.catalog.adapter.out.persistence.statistic.ProductStatisticJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.statistic.ProductStatisticJpaRepository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.support.MySqlTestContainerConfig
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig::class)
class ProductStatisticConcurrencyTest {
    @Autowired
    private lateinit var productStatisticService: DefaultProductStatisticService

    @Autowired
    private lateinit var productStatisticJpaRepository: ProductStatisticJpaRepository

    @AfterEach
    fun tearDown() {
        productStatisticJpaRepository.deleteAllInBatch()
    }

//    @Test
//    fun `좋아요 수 증가를 동시에 요청하고 결과를 출력한다`() {
//        val productId = 10L
//        val requestCount = 100
//        productStatisticJpaRepository.save(
//            ProductStatisticJpaEntity.from(
//                ProductStatistic.create(
//                    productId = productId,
//                    likeCount = 0L,
//                ),
//            ),
//        )
//
//        runConcurrently(requestCount) {
//            productStatisticService.incrementProductLikeCount(productId)
//        }
//
//        val actualLikeCount = productStatisticJpaRepository.findByProductId(productId)?.likeCount
//
//        println(
//            """
//            [increment concurrency test]
//            productId=$productId
//            requestCount=$requestCount
//            expectedLikeCount=$requestCount
//            actualLikeCount=$actualLikeCount
//            """.trimIndent(),
//        )
//    }
//
//    @Test
//    fun `좋아요 수 감소를 동시에 요청하고 결과를 출력한다`() {
//        val productId = 20L
//        val initialLikeCount = 100L
//        val requestCount = 100
//        productStatisticJpaRepository.save(
//            ProductStatisticJpaEntity.from(
//                ProductStatistic.create(
//                    productId = productId,
//                    likeCount = initialLikeCount,
//                ),
//            ),
//        )
//
//        runConcurrently(requestCount) {
//            productStatisticService.decrementProductLikeCount(productId)
//        }
//
//        val actualLikeCount = productStatisticJpaRepository.findByProductId(productId)?.likeCount
//
//        println(
//            """
//            [decrement concurrency test]
//            productId=$productId
//            initialLikeCount=$initialLikeCount
//            requestCount=$requestCount
//            expectedLikeCount=${initialLikeCount - requestCount}
//            actualLikeCount=$actualLikeCount
//            """.trimIndent(),
//        )
//    }

    @Test
    fun `update 문으로 좋아요 수 증가를 동시에 요청하고 결과를 출력한다`() {
        val productId = 30L
        val requestCount = 100
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = productId,
                    likeCount = 0L,
                ),
            ),
        )

        runConcurrently(requestCount) {
            productStatisticService.incrementProductLikeCountWithUpdate(productId)
        }

        val actualLikeCount = productStatisticJpaRepository.findByProductId(productId)?.likeCount

        println(
            """
            [increment update concurrency test]
            productId=$productId
            requestCount=$requestCount
            expectedLikeCount=$requestCount
            actualLikeCount=$actualLikeCount
            """.trimIndent(),
        )
    }

    @Test
    fun `update 문으로 좋아요 수 감소를 동시에 요청하고 결과를 출력한다`() {
        val productId = 40L
        val initialLikeCount = 100L
        val requestCount = 100
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = productId,
                    likeCount = initialLikeCount,
                ),
            ),
        )

        runConcurrently(requestCount) {
            productStatisticService.decrementProductLikeCountWithUpdate(productId)
        }

        val actualLikeCount = productStatisticJpaRepository.findByProductId(productId)?.likeCount

        println(
            """
            [decrement update concurrency test]
            productId=$productId
            initialLikeCount=$initialLikeCount
            requestCount=$requestCount
            expectedLikeCount=${initialLikeCount - requestCount}
            actualLikeCount=$actualLikeCount
            """.trimIndent(),
        )
    }

    @Test
    fun `optimistic lock로 좋아요 수 증가를 동시에 요청하고 결과를 출력한다`() {
        val productId = 50L
        val requestCount = 100
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = productId,
                    likeCount = 0L,
                ),
            ),
        )

        runConcurrently(requestCount) {
            try {
                productStatisticService.incrementProductLikeCount(productId)
            } catch (_: OptimisticLockingFailureException) {
                println("OptimisticLockingFailureException 발생")
            }
        }

        val actualStatistic = productStatisticJpaRepository.findByProductId(productId)

        println(
            """
            [increment optimistic lock concurrency test]
            productId=$productId
            requestCount=$requestCount
            expectedLikeCount=$requestCount
            actualLikeCount=${actualStatistic?.likeCount}
            actualVersion=${actualStatistic?.version}
            """.trimIndent(),
        )
    }

    @Test
    fun `optimistic lock로 좋아요 수 감소를 동시에 요청하고 결과를 출력한다`() {
        val productId = 60L
        val initialLikeCount = 100L
        val requestCount = 100
        productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(
                ProductStatistic.create(
                    productId = productId,
                    likeCount = initialLikeCount,
                ),
            ),
        )

        runConcurrently(requestCount) {
            try {
                productStatisticService.decrementProductLikeCount(productId)
            } catch (_: OptimisticLockingFailureException) {
                println("OptimisticLockingFailureException 발생")
            }
        }

        val actualStatistic = productStatisticJpaRepository.findByProductId(productId)

        println(
            """
            [decrement optimistic lock concurrency test]
            productId=$productId
            initialLikeCount=$initialLikeCount
            requestCount=$requestCount
            expectedLikeCount=${initialLikeCount - requestCount}
            actualLikeCount=${actualStatistic?.likeCount}
            actualVersion=${actualStatistic?.version}
            """.trimIndent(),
        )
    }

    private fun runConcurrently(threadCount: Int, action: () -> Unit) {
        val executor = Executors.newFixedThreadPool(threadCount)
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)

        val futures =
            (1..threadCount).map {
                executor.submit<Unit> {
                    readyLatch.countDown()
                    startLatch.await()
                    try {
                        action()
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

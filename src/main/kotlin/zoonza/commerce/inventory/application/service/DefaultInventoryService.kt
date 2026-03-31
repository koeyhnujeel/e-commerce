package zoonza.commerce.inventory.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.inventory.StockReservationSnapshot
import zoonza.commerce.inventory.StockSnapshot
import zoonza.commerce.inventory.domain.InventoryErrorCode
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.inventory.domain.StockRepository
import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

@Service
class DefaultInventoryService(
    private val stockRepository: StockRepository,
) : InventoryApi {
    @Transactional
    override fun createStock(
        productOptionId: Long,
        totalQuantity: Long,
    ): Long {
        if (stockRepository.findByProductOptionId(productOptionId) != null) {
            throw BusinessException(InventoryErrorCode.DUPLICATE_STOCK)
        }

        return stockRepository.save(Stock.create(productOptionId, totalQuantity)).id
    }

    @Transactional(readOnly = true)
    override fun getStock(productOptionId: Long): StockSnapshot {
        return stockRepository.findByProductOptionId(productOptionId)
            ?.let(StockSnapshot::from)
            ?: throw BusinessException(InventoryErrorCode.STOCK_NOT_FOUND)
    }

    @Transactional
    override fun reserve(
        productOptionId: Long,
        orderNumber: String,
        quantity: Long,
        reservedAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ): StockReservationSnapshot {
        val stock = findStockOrThrow(productOptionId)
        stock.reserve(
            orderNumber = orderNumber,
            quantity = quantity,
            reservedAt = reservedAt,
            expiresAt = expiresAt,
        )

        val savedStock = stockRepository.save(stock)
        val savedReservation = savedStock.findReservation(orderNumber)

        return StockReservationSnapshot.from(savedStock.productOptionId, savedReservation)
    }

    @Transactional
    override fun confirmReservation(
        productOptionId: Long,
        orderNumber: String,
        confirmedAt: LocalDateTime,
    ) {
        val stock = findStockOrThrow(productOptionId)
        stock.confirmReservation(orderNumber, confirmedAt)
        stockRepository.save(stock)
    }

    @Transactional
    override fun releaseReservation(
        productOptionId: Long,
        orderNumber: String,
        releasedAt: LocalDateTime,
    ) {
        val stock = findStockOrThrow(productOptionId)
        stock.releaseReservation(orderNumber, releasedAt)
        stockRepository.save(stock)
    }

    @Transactional
    override fun expireReservation(
        productOptionId: Long,
        orderNumber: String,
        expiredAt: LocalDateTime,
    ) {
        val stock = findStockOrThrow(productOptionId)
        stock.expireReservation(orderNumber, expiredAt)
        stockRepository.save(stock)
    }

    @Transactional
    override fun increaseStock(
        productOptionId: Long,
        quantity: Long,
    ) {
        val stock = findStockOrThrow(productOptionId)
        stock.increase(quantity)
        stockRepository.save(stock)
    }

    @Transactional
    override fun decreaseStock(
        productOptionId: Long,
        quantity: Long,
    ) {
        val stock = findStockOrThrow(productOptionId)
        stock.decrease(quantity)
        stockRepository.save(stock)
    }

    private fun findStockOrThrow(productOptionId: Long): Stock {
        return stockRepository.findByProductOptionId(productOptionId)
            ?: throw BusinessException(InventoryErrorCode.STOCK_NOT_FOUND)
    }
}

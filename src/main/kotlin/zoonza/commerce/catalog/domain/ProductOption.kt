package zoonza.commerce.catalog.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "product_option",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_option_product_id_option_color_option_size",
            columnNames = ["product_id", "option_color", "option_size"],
        ),
        UniqueConstraint(
            name = "uk_product_option_product_id_stock_id",
            columnNames = ["product_id", "stock_id"],
        ),
    ],
)
class ProductOption private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "option_color", nullable = false)
    val color: String,

    @Column(name = "option_size", nullable = false)
    val size: String,

    @Column(name = "stock_id", nullable = false)
    val stockId: Long,
) {
    companion object {
        fun create(
            color: String,
            size: String,
            stockId: Long,
            id: Long = 0,
        ): ProductOption {
            return ProductOption(
                id = validateId(id),
                color = normalizeColor(color),
                size = normalizeSize(size),
                stockId = validateStockId(stockId),
            )
        }

        private fun validateId(id: Long): Long {
            require(id >= 0) { "상품 옵션 ID는 0 이상이어야 합니다." }
            return id
        }

        private fun normalizeColor(color: String): String {
            require(color.isNotBlank()) { "상품 옵션 색상은 비어 있을 수 없습니다." }
            return color.trim()
        }

        private fun normalizeSize(size: String): String {
            require(size.isNotBlank()) { "상품 옵션 사이즈는 비어 있을 수 없습니다." }
            return size.trim()
        }

        private fun validateStockId(stockId: Long): Long {
            require(stockId > 0) { "재고 ID는 1 이상이어야 합니다." }
            return stockId
        }
    }
    fun isOrderable(): Boolean {
        return true
    }
}

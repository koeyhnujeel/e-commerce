package zoonza.commerce.catalog.domain.product

import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Money

class Product(
    val id: Long = 0,
    val brandId: Long,
    val name: String,
    val description: String,
    val basePrice: Money,
    val categoryId: Long,
    val saleStatus: ProductSaleStatus = ProductSaleStatus.AVAILABLE,
    val images: MutableList<ProductImage> = mutableListOf(),
    val options: MutableList<ProductOption> = mutableListOf(),
) {
    fun validateAvailableOption(productOptionId: Long) {
        if (options.none { it.id == productOptionId }) {
            throw BusinessException(ProductErrorCode.PRODUCT_OPTION_NOT_FOUND)
        }

        if (saleStatus != ProductSaleStatus.AVAILABLE) {
            throw BusinessException(ProductErrorCode.PRODUCT_UNAVAILABLE)
        }
    }
}

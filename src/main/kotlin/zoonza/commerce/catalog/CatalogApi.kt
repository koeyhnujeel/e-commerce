package zoonza.commerce.catalog

interface CatalogApi {
    fun existsProduct(id: Long): Boolean

    fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot

    fun findOrderProductSnapshot(
        productId: Long,
        productOptionId: Long,
    ): OrderProductSnapshot
}

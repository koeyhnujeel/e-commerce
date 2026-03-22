package zoonza.commerce.catalog

interface CatalogApi {
    fun assertProductExists(id: Long)

    fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot

    fun findOrderProductSnapshot(
        productId: Long,
        productOptionId: Long,
    ): OrderProductSnapshot
}

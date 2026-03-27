package zoonza.commerce.catalog

interface CatalogApi {
    fun validateProductExists(id: Long)

    fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot

    fun findOrderProductSnapshot(
        productId: Long,
        productOptionId: Long,
    ): OrderProductSnapshot
}

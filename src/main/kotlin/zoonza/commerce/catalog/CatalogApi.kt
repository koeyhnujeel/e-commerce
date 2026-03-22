package zoonza.commerce.catalog

interface CatalogApi {
    fun existsProduct(id: Long): Boolean

    fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot
}

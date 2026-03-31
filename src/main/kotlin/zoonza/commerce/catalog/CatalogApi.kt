package zoonza.commerce.catalog

interface CatalogApi {
    fun validateProductExists(id: Long)

    fun validateAvailableProductOption(
        productId: Long,
        productOptionId: Long,
    )

    fun getProductOptionSummaries(optionIds: Set<Long>): Map<Long, ProductOptionSummary>
}

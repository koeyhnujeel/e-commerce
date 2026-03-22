package zoonza.commerce.catalog.application.port.out

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductImage
import zoonza.commerce.catalog.domain.ProductOption
import zoonza.commerce.common.PageQuery
import zoonza.commerce.common.PageResult

interface ProductRepository {
    fun existsById(id: Long): Boolean

    fun findAll(
        categoryId: Long?,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<Product>

    fun findById(id: Long): Product?

    fun findPrimaryImagesByProductIds(productIds: Collection<Long>): List<ProductImage>

    fun findImagesByProductId(productId: Long): List<ProductImage>

    fun findOptionsByProductId(productId: Long): List<ProductOption>

    fun findOptionById(productOptionId: Long): ProductOption?

    fun findOptionByIdAndProductId(
        productOptionId: Long,
        productId: Long,
    ): ProductOption?
}

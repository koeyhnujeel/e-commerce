package zoonza.commerce.catalog.application.port.out

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.domain.Product

interface ProductRepository {
    fun existsById(id: Long): Boolean

    fun findById(id: Long): Product?

    fun findByOptionId(productOptionId: Long): Product?
}

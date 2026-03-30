package zoonza.commerce.catalog.domain.product

interface ProductRepository {
    fun existsById(id: Long): Boolean
}

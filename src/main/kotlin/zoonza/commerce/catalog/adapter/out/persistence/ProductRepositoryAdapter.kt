package zoonza.commerce.catalog.adapter.out.persistence

import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductImage
import zoonza.commerce.catalog.domain.ProductOption
import zoonza.commerce.common.PageQuery
import zoonza.commerce.common.PageResult
import kotlin.math.ceil

@Repository
class ProductRepositoryAdapter(
    private val entityManager: EntityManager,
    private val productJpaRepository: ProductJpaRepository,
    private val productImageJpaRepository: ProductImageJpaRepository,
    private val productOptionJpaRepository: ProductOptionJpaRepository,
) : ProductRepository {
    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun findAll(
        categoryId: Long?,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<Product> {
        val items =
            entityManager.createQuery(
                """
                select p
                from Product p
                where (:categoryId is null or :categoryId member of p.categoryIds)
                order by ${orderByClause(sort)}
                """.trimIndent(),
                Product::class.java,
            ).apply {
                setParameter("categoryId", categoryId)
                setFirstResult(pageQuery.page * pageQuery.size)
                setMaxResults(pageQuery.size)
            }.resultList

        val totalElements =
            entityManager.createQuery(
                """
                select count(p)
                from Product p
                where (:categoryId is null or :categoryId member of p.categoryIds)
                """.trimIndent(),
                java.lang.Long::class.java,
            ).apply {
                setParameter("categoryId", categoryId)
            }.singleResult.toLong()

        val totalPages =
            if (totalElements == 0L) {
                0
            } else {
                ceil(totalElements.toDouble() / pageQuery.size).toInt()
            }

        return PageResult(
            items = items,
            page = pageQuery.page,
            size = pageQuery.size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)
    }

    override fun findPrimaryImagesByProductIds(productIds: Collection<Long>): List<ProductImage> {
        if (productIds.isEmpty()) {
            return emptyList()
        }

        return productImageJpaRepository.findPrimaryImagesByProductIds(productIds)
    }

    override fun findImagesByProductId(productId: Long): List<ProductImage> {
        return productImageJpaRepository.findAllByProductId(productId)
    }

    override fun findOptionsByProductId(productId: Long): List<ProductOption> {
        return productOptionJpaRepository.findAllByProductId(productId)
    }

    override fun findOptionById(productOptionId: Long): ProductOption? {
        return productOptionJpaRepository.findByIdOrNull(productOptionId)
    }

    override fun findOptionByIdAndProductId(
        productOptionId: Long,
        productId: Long,
    ): ProductOption? {
        return productOptionJpaRepository.findByIdAndProductId(productOptionId, productId)
    }

    private fun orderByClause(sort: ProductListSort): String {
        return when (sort) {
            ProductListSort.LATEST -> "p.id desc"
            ProductListSort.PRICE_ASC -> "p.basePrice.amount asc, p.id desc"
            ProductListSort.PRICE_DESC -> "p.basePrice.amount desc, p.id desc"
        }
    }
}

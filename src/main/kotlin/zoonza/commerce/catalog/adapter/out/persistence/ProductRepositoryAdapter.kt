package zoonza.commerce.catalog.adapter.out.persistence

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductImage
import zoonza.commerce.catalog.domain.ProductOption
import zoonza.commerce.catalog.domain.QProduct.Companion.product
import zoonza.commerce.common.PageQuery
import zoonza.commerce.common.PageResult
import kotlin.math.ceil

@Repository
class ProductRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
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
            queryFactory
                .selectFrom(product)
                .where(categoryIdEq(categoryId))
                .orderBy(*orderSpecifiers(sort))
                .offset((pageQuery.page * pageQuery.size).toLong())
                .limit(pageQuery.size.toLong())
                .fetch()

        val totalElements =
            queryFactory
                .select(product.count())
                .from(product)
                .where(categoryIdEq(categoryId))
                .fetchOne() ?: 0L

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

    private fun categoryIdEq(categoryId: Long?): BooleanExpression? {
        return categoryId?.let(product.categoryIds.any()::eq)
    }

    private fun orderSpecifiers(sort: ProductListSort): Array<OrderSpecifier<*>> {
        return when (sort) {
            ProductListSort.LATEST -> arrayOf(product.id.desc())
            ProductListSort.PRICE_ASC -> arrayOf(product.basePrice.amount.asc(), product.id.desc())
            ProductListSort.PRICE_DESC -> arrayOf(product.basePrice.amount.desc(), product.id.desc())
        }
    }
}

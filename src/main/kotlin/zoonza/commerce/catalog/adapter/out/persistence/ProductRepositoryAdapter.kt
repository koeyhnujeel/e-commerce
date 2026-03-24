package zoonza.commerce.catalog.adapter.out.persistence

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.QProduct.Companion.product
import zoonza.commerce.catalog.domain.QProductImage.Companion.productImage
import zoonza.commerce.catalog.domain.QProductOption.Companion.productOption
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult
import kotlin.math.ceil

@Repository
class ProductRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
    private val productJpaRepository: ProductJpaRepository,
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

    override fun findPrimaryImageUrlsByProductIds(productIds: Collection<Long>): Map<Long, String> {
        if (productIds.isEmpty()) {
            return emptyMap()
        }

        return queryFactory
            .select(product.id, productImage.imageUrl)
            .from(product)
            .join(product.images, productImage)
            .where(
                product.id.`in`(productIds),
                productImage.isPrimary.isTrue,
            )
            .fetch()
            .associate { tuple ->
                tuple.get(product.id)!! to tuple.get(productImage.imageUrl)!!
            }
    }

    override fun findByOptionId(productOptionId: Long): Product? {
        return queryFactory
            .selectDistinct(product)
            .from(product)
            .join(product.options, productOption)
            .where(productOption.id.eq(productOptionId))
            .fetchOne()
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

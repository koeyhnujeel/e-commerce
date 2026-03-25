package zoonza.commerce.catalog.adapter.out.persistence

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryRepository
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryResult
import zoonza.commerce.catalog.domain.ProductSaleStatus
import zoonza.commerce.catalog.domain.QProduct.Companion.product
import zoonza.commerce.catalog.domain.QProductImage.Companion.productImage
import zoonza.commerce.catalog.domain.QProductStatistic.Companion.productStatistic
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult
import kotlin.math.ceil

@Repository
class ProductSummaryQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : ProductSummaryQueryRepository {
    override fun findPageByCategoryIds(
        categoryIds: Set<Long>?,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<ProductSummaryQueryResult> {
        if (categoryIds?.isEmpty() == true) {
            return PageResult(
                items = emptyList(),
                page = pageQuery.page,
                size = pageQuery.size,
                totalElements = 0L,
                totalPages = 0,
            )
        }

        val items = queryFactory
            .select(
                QProductSummaryProjection(
                    product.id,
                    product.name,
                    productImage.imageUrl,
                    product.basePrice.amount,
                    productStatistic.likeCount.coalesce(0L),
                    Expressions.constant(ProductSaleStatus.AVAILABLE),
                ),
            )
            .from(product)
            .join(product.images, productImage)
            .leftJoin(productStatistic).on(productStatistic.productId.eq(product.id))
            .where(
                categoryIdsIn(categoryIds),
                productImage.isPrimary.isTrue,
            )
            .orderBy(*orderSpecifiers(sort))
            .offset((pageQuery.page * pageQuery.size).toLong())
            .limit(pageQuery.size.toLong())
            .fetch()

        val totalElements = queryFactory
            .select(product.count())
            .from(product)
            .where(categoryIdsIn(categoryIds))
            .fetchOne() ?: 0L

        val totalPages =
            if (totalElements == 0L) {
                0
            } else {
                ceil(totalElements.toDouble() / pageQuery.size).toInt()
            }

        return PageResult(
            items = items.map { summary ->
                ProductSummaryQueryResult(
                    productId = summary.productId,
                    name = summary.name,
                    primaryImageUrl = summary.primaryImageUrl,
                    basePrice = summary.basePrice,
                    likeCount = summary.likeCount,
                    saleStatus = summary.saleStatus,
                )
            },
            page = pageQuery.page,
            size = pageQuery.size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    private fun categoryIdsIn(categoryIds: Set<Long>?): BooleanExpression? {
        return categoryIds?.let(product.categoryIds.any()::`in`)
    }

    private fun orderSpecifiers(sort: ProductListSort): Array<OrderSpecifier<*>> {
        return when (sort) {
            ProductListSort.LATEST -> arrayOf(product.id.desc())
            ProductListSort.PRICE_ASC -> arrayOf(product.basePrice.amount.asc(), product.id.desc())
            ProductListSort.PRICE_DESC -> arrayOf(product.basePrice.amount.desc(), product.id.desc())
        }
    }
}

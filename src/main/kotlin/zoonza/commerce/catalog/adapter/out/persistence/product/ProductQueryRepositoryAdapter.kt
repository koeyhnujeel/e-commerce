package zoonza.commerce.catalog.adapter.out.persistence.product

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.adapter.out.persistence.product.QProductJpaEntity.Companion.productJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.QProductImageJpaEntity.Companion.productImageJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.QProductOptionJpaEntity.Companion.productOptionJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductDetailProjection
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductImageDetailProjection
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductOptionDetailProjection
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductSummaryProjection
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductDetailQueryResult
import zoonza.commerce.catalog.application.port.out.ProductImageQueryResult
import zoonza.commerce.catalog.application.port.out.ProductOptionQueryResult
import zoonza.commerce.catalog.application.port.out.ProductQueryRepository
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryResult
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.catalog.domain.statistic.QProductStatistic.Companion.productStatistic
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult
import kotlin.math.ceil

@Repository
class ProductQueryRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
) : ProductQueryRepository {
    override fun findProductDetailsById(id: Long): ProductDetailQueryResult? {
        val productDetail = queryFactory
            .select(
                QProductDetailProjection(
                    productJpaEntity.id,
                    productJpaEntity.name,
                    productJpaEntity.description,
                    productJpaEntity.basePrice.amount,
                    productJpaEntity.categoryId,
                    productStatistic.likeCount.coalesce(0L),
                ),
            )
            .from(productJpaEntity)
            .leftJoin(productStatistic).on(productStatistic.productId.eq(productJpaEntity.id))
            .where(productJpaEntity.id.eq(id))
            .fetchOne()
            ?: return null

        val images = queryFactory
            .select(
                QProductImageDetailProjection(
                    productImageJpaEntity.imageUrl,
                    productImageJpaEntity.isPrimary,
                    productImageJpaEntity.sortOrder,
                ),
            )
            .from(productJpaEntity)
            .join(productJpaEntity.images, productImageJpaEntity)
            .where(productJpaEntity.id.eq(id))
            .orderBy(productImageJpaEntity.sortOrder.asc())
            .fetch()

        val options = queryFactory
            .select(
                QProductOptionDetailProjection(
                    productOptionJpaEntity.id,
                    productOptionJpaEntity.color,
                    productOptionJpaEntity.size,
                    productOptionJpaEntity.sortOrder,
                    productOptionJpaEntity.additionalPrice.amount,
                ),
            )
            .from(productJpaEntity)
            .join(productJpaEntity.options, productOptionJpaEntity)
            .where(productJpaEntity.id.eq(id))
            .orderBy(productOptionJpaEntity.sortOrder.asc())
            .fetch()

        return ProductDetailQueryResult(
            productId = productDetail.productId,
            name = productDetail.name,
            description = productDetail.description,
            basePrice = productDetail.basePrice,
            categoryId = productDetail.categoryId,
            images = images.map { image ->
                ProductImageQueryResult(
                    imageUrl = image.imageUrl,
                    isPrimary = image.isPrimary,
                    sortOrder = image.sortOrder,
                )
            },
            options = options.map { option ->
                ProductOptionQueryResult(
                    productOptionId = option.productOptionId,
                    color = option.color,
                    size = option.size,
                    sortOrder = option.sortOrder,
                    additionalPrice = option.additionalPrice,
                )
            },
            likeCount = productDetail.likeCount,
        )
    }

    override fun findPageByCategoryIds(
        categoryIds: Set<Long>,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<ProductSummaryQueryResult> {
        if (categoryIds.isEmpty()) {
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
                    productJpaEntity.id,
                    productJpaEntity.name,
                    productImageJpaEntity.imageUrl,
                    productJpaEntity.basePrice.amount,
                    productStatistic.likeCount.coalesce(0L),
                    Expressions.constant(ProductSaleStatus.AVAILABLE),
                ),
            )
            .from(productJpaEntity)
            .join(productJpaEntity.images, productImageJpaEntity)
            .leftJoin(productStatistic).on(productStatistic.productId.eq(productJpaEntity.id))
            .where(
                categoryIdsIn(categoryIds),
                productImageJpaEntity.isPrimary.isTrue,
            )
            .orderBy(*orderSpecifiers(sort))
            .offset((pageQuery.page * pageQuery.size).toLong())
            .limit(pageQuery.size.toLong())
            .fetch()

        val totalElements = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
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

    private fun categoryIdsIn(categoryIds: Set<Long>): BooleanExpression {
        return productJpaEntity.categoryId.`in`(categoryIds)
    }

    private fun orderSpecifiers(sort: ProductListSort): Array<OrderSpecifier<*>> {
        return when (sort) {
            ProductListSort.LATEST -> arrayOf(productJpaEntity.id.desc())
            ProductListSort.PRICE_ASC -> arrayOf(productJpaEntity.basePrice.amount.asc(), productJpaEntity.id.desc())
            ProductListSort.PRICE_DESC -> arrayOf(productJpaEntity.basePrice.amount.desc(), productJpaEntity.id.desc())
        }
    }
}

package zoonza.commerce.catalog.adapter.out.persistence.product

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.adapter.out.persistence.brand.QBrandJpaEntity.Companion.brandJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.QProductImageJpaEntity.Companion.productImageJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.QProductJpaEntity.Companion.productJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.QProductOptionJpaEntity.Companion.productOptionJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductDetailProjection
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductImageDetailProjection
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductOptionDetailProjection
import zoonza.commerce.catalog.adapter.out.persistence.product.projection.QProductSummaryProjection
import zoonza.commerce.catalog.adapter.out.persistence.statistic.QProductStatisticJpaEntity.Companion.productStatisticJpaEntity
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.*
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
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
                    brandJpaEntity.name,
                    productJpaEntity.description,
                    productJpaEntity.basePrice,
                    productJpaEntity.categoryId,
                    productStatisticJpaEntity.likeCount.coalesce(0L),
                ),
            )
            .from(productJpaEntity)
            .join(brandJpaEntity).on(brandJpaEntity.id.eq(productJpaEntity.brandId))
            .leftJoin(productStatisticJpaEntity).on(productStatisticJpaEntity.productId.eq(productJpaEntity.id))
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
                    productOptionJpaEntity.additionalPrice,
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
            brandName = productDetail.brandName,
            description = productDetail.description,
            basePrice = productDetail.basePrice.longValueExact(),
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
                    additionalPrice = option.additionalPrice.longValueExact(),
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
                    brandJpaEntity.name,
                    productImageJpaEntity.imageUrl,
                    productJpaEntity.basePrice,
                    productStatisticJpaEntity.likeCount.coalesce(0L),
                    Expressions.constant(ProductSaleStatus.AVAILABLE),
                ),
            )
            .from(productJpaEntity)
            .join(brandJpaEntity).on(brandJpaEntity.id.eq(productJpaEntity.brandId))
            .join(productJpaEntity.images, productImageJpaEntity)
            .leftJoin(productStatisticJpaEntity).on(productStatisticJpaEntity.productId.eq(productJpaEntity.id))
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
            .join(brandJpaEntity).on(brandJpaEntity.id.eq(productJpaEntity.brandId))
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
                    brandName = summary.brandName,
                    primaryImageUrl = summary.primaryImageUrl,
                    basePrice = summary.basePrice.longValueExact(),
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
            ProductListSort.PRICE_ASC -> arrayOf(productJpaEntity.basePrice.asc(), productJpaEntity.id.desc())
            ProductListSort.PRICE_DESC -> arrayOf(productJpaEntity.basePrice.desc(), productJpaEntity.id.desc())
        }
    }
}

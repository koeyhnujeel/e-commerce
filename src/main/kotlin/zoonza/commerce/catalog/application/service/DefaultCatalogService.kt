package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.OrderProductSnapshot
import zoonza.commerce.catalog.ProductOptionSnapshot
import zoonza.commerce.catalog.application.dto.ProductDetail
import zoonza.commerce.catalog.application.dto.ProductImageDetail
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.dto.ProductOptionDetail
import zoonza.commerce.catalog.application.dto.ProductSummary
import zoonza.commerce.catalog.application.port.out.CategoryHierarchyRepository
import zoonza.commerce.catalog.application.port.`in`.CatalogService
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.application.port.out.ProductStatisticRepository
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResponse
import zoonza.commerce.like.LikeApi
import zoonza.commerce.catalog.CatalogErrorCode
import zoonza.commerce.shared.BusinessException

@Service
class DefaultCatalogService(
    private val productRepository: ProductRepository,
    private val productSummaryQueryRepository: ProductSummaryQueryRepository,
    private val categoryHierarchyRepository: CategoryHierarchyRepository,
    private val productStatisticRepository: ProductStatisticRepository,
    private val likeApi: LikeApi,
) : CatalogApi, CatalogService {
    override fun assertProductExists(id: Long) {
        if (!productRepository.existsById(id)) {
            throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)
        }
    }

    @Transactional(readOnly = true)
    override fun getProductsByCategory(
        memberId: Long?,
        page: Int,
        size: Int,
        categoryId: Long?,
        sort: ProductListSort,
    ): PageResponse<ProductSummary> {
        val categoryIds = categoryId?.let(categoryHierarchyRepository::findSelfAndDescendantIds)
        val productPage = productSummaryQueryRepository.findPageByCategoryIds(
            categoryIds = categoryIds,
            pageQuery = PageQuery(page = page, size = size),
            sort = sort,
        )
        val productIds = productPage.items.map { it.productId }
        val likedProductIds = likedProductIds(memberId, productIds)

        return PageResponse(
            items = productPage.items.map { product ->
                ProductSummary(
                    productId = product.productId,
                    name = product.name,
                    primaryImageUrl = product.primaryImageUrl,
                    basePrice = product.basePrice,
                    likeCount = product.likeCount,
                    likedByMe = product.productId in likedProductIds,
                    saleStatus = product.saleStatus,
                )
            },
            page = productPage.page,
            size = productPage.size,
            totalElements = productPage.totalElements,
            totalPages = productPage.totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun getProduct(
        productId: Long,
        memberId: Long?,
    ): ProductDetail {
        val product = productRepository.findById(productId)
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)

        if (!product.isAvailableForSale()) {
            throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)
        }

        val likedByMe = memberId?.let { productId in likeApi.findLikedProductIds(it, listOf(productId)) } ?: false
        val likeCount = productStatisticRepository.findLikeCount(productId)

        return ProductDetail(
            productId = product.id,
            name = product.name,
            description = product.description,
            basePrice = product.basePrice.amount,
            categoryIds = product.categoryIds.toList().sorted(),
            images = product.images.map { image ->
                ProductImageDetail(
                    imageUrl = image.imageUrl,
                    isPrimary = image.isPrimary,
                    sortOrder = image.sortOrder,
                )
            },
            options = product.options.map { option ->
                ProductOptionDetail(
                    productOptionId = option.id,
                    color = option.color,
                    size = option.size,
                    stockId = option.stockId,
                    orderable = option.isOrderable(),
                )
            },
            likeCount = likeCount,
            likedByMe = likedByMe,
            saleStatus = product.saleStatus(),
        )
    }

    override fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot {
        val product = productRepository.findByOptionId(productOptionId)
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_OPTION_NOT_FOUND)
        val option = product.options.firstOrNull { it.id == productOptionId }
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_OPTION_NOT_FOUND)

        return ProductOptionSnapshot(
            color = option.color,
            size = option.size,
        )
    }

    override fun findOrderProductSnapshot(
        productId: Long,
        productOptionId: Long,
    ): OrderProductSnapshot {
        val product = productRepository.findById(productId)
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)

        if (!product.isAvailableForSale()) {
            throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)
        }

        val option = product.options.firstOrNull { it.id == productOptionId }
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_OPTION_NOT_FOUND)

        if (!option.isOrderable()) {
            throw BusinessException(CatalogErrorCode.PRODUCT_OPTION_NOT_FOUND)
        }

        return OrderProductSnapshot(
            productName = product.name,
            option = ProductOptionSnapshot(color = option.color, size = option.size),
            unitPrice = product.basePrice,
        )
    }

    private fun likedProductIds(
        memberId: Long?,
        productIds: List<Long>,
    ): Set<Long> {
        if (memberId == null) {
            return emptySet()
        }

        return likeApi.findLikedProductIds(memberId, productIds)
    }
}

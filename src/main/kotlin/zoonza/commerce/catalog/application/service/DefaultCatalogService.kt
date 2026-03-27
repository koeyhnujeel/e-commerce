package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.CatalogErrorCode
import zoonza.commerce.catalog.OrderProductSnapshot
import zoonza.commerce.catalog.ProductOptionSnapshot
import zoonza.commerce.catalog.application.dto.*
import zoonza.commerce.catalog.application.port.`in`.CatalogService
import zoonza.commerce.catalog.application.port.out.ProductQueryRepository
import zoonza.commerce.catalog.domain.category.CategoryRepository
import zoonza.commerce.catalog.domain.product.ProductRepository
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.like.LikeApi
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResponse

@Service
class DefaultCatalogService(
    private val productRepository: ProductRepository,
    private val productQueryRepository: ProductQueryRepository,
    private val categoryRepository: CategoryRepository,
    private val likeApi: LikeApi,
) : CatalogApi, CatalogService {
    @Transactional(readOnly = true)
    override fun getProductsByCategory(
        memberId: Long?,
        page: Int,
        size: Int,
        categoryId: Long,
        sort: ProductListSort,
    ): PageResponse<ProductSummary> {
        val categoryIds = categoryRepository.findAllDescendantIds(categoryId)
        val productPage = productQueryRepository.findPageByCategoryIds(
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
    override fun getProductDetails(
        productId: Long,
        memberId: Long?,
    ): ProductDetail {
        val product = productQueryRepository.findProductDetailsById(productId)
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)

        val saleStatus =
            if (product.options.isNotEmpty()) {
                ProductSaleStatus.AVAILABLE
            } else {
                ProductSaleStatus.UNAVAILABLE
            }

        if (saleStatus != ProductSaleStatus.AVAILABLE) {
            throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)
        }

        val likedByMe = memberId?.let { productId in likeApi.findLikedProductIds(it, listOf(productId)) } ?: false

        return ProductDetail(
            productId = product.productId,
            name = product.name,
            description = product.description,
            basePrice = product.basePrice,
            categoryId = product.categoryId,
            images = product.images.map { image ->
                ProductImageDetail(
                    imageUrl = image.imageUrl,
                    isPrimary = image.isPrimary,
                    sortOrder = image.sortOrder,
                )
            },
            options = product.options.map { option ->
                ProductOptionDetail(
                    productOptionId = option.productOptionId,
                    color = option.color,
                    size = option.size,
                    sortOrder = option.sortOrder,
                    additionalPrice = option.additionalPrice,
                )
            },
            likeCount = product.likeCount,
            likedByMe = likedByMe,
            saleStatus = saleStatus,
        )
    }

    override fun validateProductExists(id: Long) {
        if (!productRepository.existsById(id)) {
            throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)
        }
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

        val option = product.options.firstOrNull { it.id == productOptionId }
            ?: throw BusinessException(CatalogErrorCode.PRODUCT_OPTION_NOT_FOUND)

        return OrderProductSnapshot(
            productName = product.name,
            option = ProductOptionSnapshot(color = option.color, size = option.size),
            unitPrice = product.basePrice + option.additionalPrice,
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

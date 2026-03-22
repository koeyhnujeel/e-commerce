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
import zoonza.commerce.catalog.application.port.`in`.CatalogService
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.common.PageQuery
import zoonza.commerce.common.PageResponse
import zoonza.commerce.like.LikeApi
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode

@Service
class DefaultCatalogService(
    private val productRepository: ProductRepository,
    private val likeApi: LikeApi,
) : CatalogApi, CatalogService {
    override fun existsProduct(id: Long): Boolean {
        return productRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    override fun getProducts(
        memberId: Long?,
        page: Int,
        size: Int,
        categoryId: Long?,
        sort: ProductListSort,
    ): PageResponse<ProductSummary> {
        val productPage = productRepository.findAll(
            categoryId = categoryId,
            pageQuery = PageQuery(page = page, size = size),
            sort = sort,
        )
        val productIds = productPage.items.map(Product::id)
        val primaryImageUrls =
            productRepository.findPrimaryImagesByProductIds(productIds)
                .associate { productImage ->
                    val productId = productImage.product?.id
                        ?: throw IllegalStateException("상품 이미지에 상품 정보가 없습니다.")

                    productId to productImage.imageUrl
                }
        val likeCounts = likeApi.countProductLikes(productIds.toList())
        val likedProductIds = likedProductIds(memberId, productIds)

        return PageResponse(
            items = productPage.items.map { product ->
                ProductSummary(
                    productId = product.id,
                    name = product.name,
                    primaryImageUrl = primaryImageUrls[product.id]
                        ?: throw IllegalStateException("대표 상품 이미지를 찾을 수 없습니다."),
                    basePrice = product.basePrice.amount,
                    likeCount = likeCounts[product.id] ?: 0L,
                    likedByMe = product.id in likedProductIds,
                    saleStatus = product.saleStatus(),
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
            ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

        if (!product.isAvailableForSale()) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }

        val images = productRepository.findImagesByProductId(productId)
        val options = productRepository.findOptionsByProductId(productId)
        val likedByMe = memberId?.let { productId in likeApi.findLikedProductIds(it, listOf(productId)) } ?: false

        return ProductDetail(
            productId = product.id,
            name = product.name,
            description = product.description,
            basePrice = product.basePrice.amount,
            categoryIds = product.categoryIds.toList().sorted(),
            images = images.map { image ->
                ProductImageDetail(
                    imageUrl = image.imageUrl,
                    isPrimary = image.isPrimary,
                    sortOrder = image.sortOrder,
                )
            },
            options = options.map { option ->
                ProductOptionDetail(
                    productOptionId = option.id,
                    color = option.color,
                    size = option.size,
                    stockId = option.stockId,
                    orderable = option.isOrderable(),
                )
            },
            likeCount = likeApi.countProductLikes(productId),
            likedByMe = likedByMe,
            saleStatus = product.saleStatus(),
        )
    }

    override fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot {
        val option = productRepository.findOptionById(productOptionId)
            ?: throw BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)

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
            ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

        if (!product.isAvailableForSale()) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }

        val option = productRepository.findOptionByIdAndProductId(productOptionId, productId)
            ?: throw BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)

        if (!option.isOrderable()) {
            throw BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)
        }

        return OrderProductSnapshot(
            productName = product.name,
            optionColor = option.color,
            optionSize = option.size,
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

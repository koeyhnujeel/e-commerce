package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.application.dto.*
import zoonza.commerce.catalog.application.port.`in`.ProductService
import zoonza.commerce.catalog.application.port.out.ProductQueryRepository
import zoonza.commerce.catalog.domain.category.CategoryErrorCode
import zoonza.commerce.catalog.domain.category.CategoryRepository
import zoonza.commerce.catalog.domain.product.ProductErrorCode
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResponse

@Service
class DefaultProductService(
    private val productQueryRepository: ProductQueryRepository,
    private val categoryRepository: CategoryRepository,
) : ProductService {
    @Transactional(readOnly = true)
    override fun getCategoryProducts(
        page: Int,
        size: Int,
        categoryId: Long,
        sort: ProductListSort,
    ): PageResponse<ProductSummary> {
        val categoryIds = categoryRepository.findSelfAndSubCategoryIds(categoryId)

        if (categoryIds.isEmpty()) {
            throw BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND)
        }

        val productPage = productQueryRepository.findPageByCategoryIds(categoryIds, PageQuery(page, size), sort)

        return PageResponse(
            items = productPage.items.map { product ->
                ProductSummary(
                    productId = product.productId,
                    name = product.name,
                    brandName = product.brandName,
                    primaryImageUrl = product.primaryImageUrl,
                    basePrice = product.basePrice,
                    likeCount = product.likeCount,
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
    ): ProductDetail {
        val product = productQueryRepository.findProductDetailsById(productId)
            ?: throw BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)

        val saleStatus =
            if (product.options.isNotEmpty()) {
                ProductSaleStatus.AVAILABLE
            } else {
                ProductSaleStatus.UNAVAILABLE
            }

        if (saleStatus != ProductSaleStatus.AVAILABLE) {
            throw BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)
        }

        return ProductDetail(
            productId = product.productId,
            name = product.name,
            brandName = product.brandName,
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
            saleStatus = saleStatus,
        )
    }
}

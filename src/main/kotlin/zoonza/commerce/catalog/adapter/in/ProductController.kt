package zoonza.commerce.catalog.adapter.`in`

import jakarta.validation.constraints.Positive
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.catalog.adapter.`in`.response.ProductDetailResponse
import zoonza.commerce.catalog.adapter.`in`.response.ProductImageResponse
import zoonza.commerce.catalog.adapter.`in`.response.ProductOptionResponse
import zoonza.commerce.catalog.adapter.`in`.response.ProductSummaryResponse
import zoonza.commerce.catalog.application.dto.ProductDetail
import zoonza.commerce.catalog.application.dto.ProductImageDetail
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.dto.ProductOptionDetail
import zoonza.commerce.catalog.application.dto.ProductSummary
import zoonza.commerce.catalog.application.port.`in`.CatalogService
import zoonza.commerce.support.web.ApiResponse
import zoonza.commerce.support.pagination.PageResponse
import zoonza.commerce.security.CurrentMember

@Validated
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val catalogService: CatalogService,
) {
    @GetMapping
    fun getProductsByCategory(
        @RequestParam(defaultValue = "1")
        @Positive(message = "페이지 번호는 1 이상이어야 합니다.")
        page: Int,
        @RequestParam(defaultValue = "20")
        @Positive(message = "페이지 크기는 1 이상이어야 합니다.")
        size: Int,
        @RequestParam(required = false)
        @Positive(message = "카테고리 ID는 1 이상이어야 합니다.")
        categoryId: Long?,
        @RequestParam(defaultValue = "LATEST")
        sort: ProductListSort,
        @AuthenticationPrincipal currentMember: CurrentMember?,
    ): ApiResponse<PageResponse<ProductSummaryResponse>> {
        val products = catalogService.getProductsByCategory(
            memberId = currentMember?.memberId,
            page = page - 1,
            size = size,
            categoryId = categoryId,
            sort = sort,
        )

        return ApiResponse.success(
            PageResponse(
                items = products.items.map(::toProductSummaryResponse),
                page = products.page + 1,
                size = products.size,
                totalElements = products.totalElements,
                totalPages = products.totalPages,
            ),
        )
    }

    @GetMapping("/{productId}")
    fun getProduct(
        @PathVariable productId: Long,
        @AuthenticationPrincipal currentMember: CurrentMember?,
    ): ApiResponse<ProductDetailResponse> {
        val product = catalogService.getProduct(
            productId = productId,
            memberId = currentMember?.memberId,
        )

        return ApiResponse.success(toProductDetailResponse(product))
    }

    private fun toProductSummaryResponse(product: ProductSummary): ProductSummaryResponse {
        return ProductSummaryResponse(
            productId = product.productId,
            name = product.name,
            primaryImageUrl = product.primaryImageUrl,
            basePrice = product.basePrice,
            likeCount = product.likeCount,
            likedByMe = product.likedByMe,
            saleStatus = product.saleStatus,
        )
    }

    private fun toProductDetailResponse(product: ProductDetail): ProductDetailResponse {
        return ProductDetailResponse(
            productId = product.productId,
            name = product.name,
            description = product.description,
            basePrice = product.basePrice,
            categoryIds = product.categoryIds,
            images = product.images.map(::toProductImageResponse),
            options = product.options.map(::toProductOptionResponse),
            likeCount = product.likeCount,
            likedByMe = product.likedByMe,
            saleStatus = product.saleStatus,
        )
    }

    private fun toProductImageResponse(image: ProductImageDetail): ProductImageResponse {
        return ProductImageResponse(
            imageUrl = image.imageUrl,
            isPrimary = image.isPrimary,
            sortOrder = image.sortOrder,
        )
    }

    private fun toProductOptionResponse(option: ProductOptionDetail): ProductOptionResponse {
        return ProductOptionResponse(
            productOptionId = option.productOptionId,
            color = option.color,
            size = option.size,
            stockId = option.stockId,
            orderable = option.orderable,
        )
    }
}

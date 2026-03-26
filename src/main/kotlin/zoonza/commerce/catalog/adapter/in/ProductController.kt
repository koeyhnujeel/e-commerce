package zoonza.commerce.catalog.adapter.`in`

import jakarta.validation.constraints.Positive
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import zoonza.commerce.catalog.adapter.`in`.response.ProductDetailResponse
import zoonza.commerce.catalog.adapter.`in`.response.ProductSummaryResponse
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.`in`.CatalogService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.pagination.PageResponse
import zoonza.commerce.support.web.ApiResponse

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
        @RequestParam
        @Positive(message = "카테고리 ID는 1 이상이어야 합니다.")
        categoryId: Long,
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
                items = products.items.map(ProductSummaryResponse::from),
                page = products.page + 1,
                size = products.size,
                totalElements = products.totalElements,
                totalPages = products.totalPages,
            ),
        )
    }

    @GetMapping("/{productId}")
    fun getProductDetails(
        @PathVariable productId: Long,
        @AuthenticationPrincipal currentMember: CurrentMember?,
    ): ApiResponse<ProductDetailResponse> {
        val product = catalogService.getProductDetails(
            productId = productId,
            memberId = currentMember?.memberId,
        )

        return ApiResponse.success(ProductDetailResponse.from(product))
    }
}

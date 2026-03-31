package zoonza.commerce.catalog.adapter.`in`

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.catalog.adapter.`in`.response.CategoryRootResponse
import zoonza.commerce.catalog.adapter.`in`.response.CategoryTreeResponse
import zoonza.commerce.catalog.adapter.`in`.response.SubCategoryResponse
import zoonza.commerce.catalog.application.port.`in`.CategoryService
import zoonza.commerce.support.web.ApiResponse

@RestController
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping("/api/categories")
    fun getAllCategories(): ApiResponse<List<CategoryTreeResponse>> {
        val categories = categoryService.getAllCategories()

        return ApiResponse.success(categories.map(CategoryTreeResponse::from))
    }

    @GetMapping("/api/categories/roots")
    fun getRootCategories(): ApiResponse<List<CategoryRootResponse>> {
        val categories = categoryService.getRootCategories()

        return ApiResponse.success(categories.map(CategoryRootResponse::from))
    }

    @GetMapping("/api/categories/{categoryId}/sub-categories")
    fun getSubCategories(
        @PathVariable categoryId: Long,
    ): ApiResponse<List<SubCategoryResponse>> {
        val subCategories = categoryService.getSubCategories(categoryId)

        return ApiResponse.success(subCategories.map(SubCategoryResponse::from))
    }
}

package zoonza.commerce.catalog.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.domain.category.Category
import zoonza.commerce.catalog.domain.category.CategoryErrorCode
import zoonza.commerce.catalog.domain.category.CategoryRepository
import zoonza.commerce.shared.BusinessException

class DefaultCategoryServiceTest {
    private val categoryRepository = mockk<CategoryRepository>()
    private val categoryService = DefaultCategoryService(categoryRepository)

    @Test
    fun `루트 카테고리 조회는 루트만 정렬해서 반환한다`() {
        every { categoryRepository.findRootCategories() } returns listOf(
            category(id = 1L, name = "의류", depth = 0, sortOrder = 0),
            category(id = 2L, name = "신발", depth = 0, sortOrder = 1),
        )

        val result = categoryService.getRootCategories()

        result.map { it.id } shouldBe listOf(1L, 2L)
        result.map { it.name } shouldBe listOf("의류", "신발")
        verify(exactly = 0) { categoryRepository.findAll() }
    }

    @Test
    fun `전체 카테고리 조회는 root와 sub 2단계만 반환한다`() {
        every { categoryRepository.findAll() } returns listOf(
            category(id = 1L, name = "의류", depth = 0, sortOrder = 0),
            category(id = 2L, name = "신발", depth = 0, sortOrder = 1),
            category(id = 10L, name = "상의", parentId = 1L, depth = 1, sortOrder = 0),
            category(id = 11L, name = "하의", parentId = 1L, depth = 1, sortOrder = 1),
        )

        val result = categoryService.getAllCategories()

        result.map { it.name } shouldBe listOf("의류", "신발")
        result.first().sub.map { it.name } shouldBe listOf("상의", "하의")
        result.first().sub.flatMap { it.sub } shouldBe emptyList()
    }

    @Test
    fun `루트 카테고리의 하위 카테고리 조회는 직계 하위만 반환한다`() {
        every { categoryRepository.findById(1L) } returns category(id = 1L, name = "의류", depth = 0, sortOrder = 0)
        every { categoryRepository.findSubCategories(1L) } returns listOf(
            category(id = 10L, name = "상의", parentId = 1L, depth = 1, sortOrder = 0),
            category(id = 11L, name = "하의", parentId = 1L, depth = 1, sortOrder = 1),
        )

        val result = categoryService.getSubCategories(1L)

        result.map { it.id } shouldBe listOf(10L, 11L)
        result.map { it.name } shouldBe listOf("상의", "하의")
        verify(exactly = 0) { categoryRepository.findAll() }
    }

    @Test
    fun `하위 카테고리가 없는 루트 카테고리 조회는 빈 리스트를 반환한다`() {
        every { categoryRepository.findById(1L) } returns category(id = 1L, name = "의류", depth = 0, sortOrder = 0)
        every { categoryRepository.findSubCategories(1L) } returns emptyList()

        val result = categoryService.getSubCategories(1L)

        result shouldBe emptyList()
    }

    @Test
    fun `sub 카테고리의 하위 카테고리 조회는 예외를 던진다`() {
        every { categoryRepository.findById(11L) } returns category(id = 11L, name = "하의", parentId = 1L, depth = 1, sortOrder = 1)

        val exception = shouldThrow<BusinessException> {
            categoryService.getSubCategories(11L)
        }

        exception.errorCode shouldBe CategoryErrorCode.ROOT_CATEGORY_REQUIRED
    }

    @Test
    fun `없는 카테고리의 하위 카테고리 조회는 예외를 던진다`() {
        every { categoryRepository.findById(999L) } returns null

        val exception = shouldThrow<BusinessException> {
            categoryService.getSubCategories(999L)
        }

        exception.errorCode shouldBe CategoryErrorCode.CATEGORY_NOT_FOUND
    }

    private fun category(
        id: Long,
        name: String,
        depth: Int,
        sortOrder: Int,
        parentId: Long? = null,
    ): Category {
        return Category(
            id = id,
            name = name,
            rootCategoryId = parentId,
            depth = depth,
            sortOrder = sortOrder,
        )
    }
}

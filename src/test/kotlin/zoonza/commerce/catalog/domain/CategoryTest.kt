package zoonza.commerce.catalog.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.domain.category.Category

class CategoryTest {
    @Test
    fun `루트 카테고리는 parentId가 없다`() {
        val category = Category(
            id = 1L,
            name = "  상의  ",
            rootCategoryId = null,
            depth = 0,
            sortOrder = 0,
        )

        category.name shouldBe "  상의  "
        category.rootCategoryId shouldBe null
        category.isRoot() shouldBe true
    }

    @Test
    fun `자식 카테고리는 루트가 아니다`() {
        val child = Category(
            id = 2L,
            rootCategoryId = 1L,
            name = "티셔츠",
            depth = 1,
            sortOrder = 0,
        )

        child.rootCategoryId shouldBe 1L
        child.depth shouldBe 1
        child.isRoot() shouldBe false
    }

    @Test
    fun `카테고리 depth는 2단계를 넘을 수 없다`() {
        shouldThrow<IllegalArgumentException> {
            Category(
                id = 3L,
                rootCategoryId = 2L,
                name = "니트",
                depth = 2,
                sortOrder = 0,
            )
        }
    }

    @Test
    fun `루트 카테고리는 parentId가 있으면 안 된다`() {
        shouldThrow<IllegalArgumentException> {
            Category(
                id = 4L,
                rootCategoryId = 1L,
                name = "상의",
                depth = 0,
                sortOrder = 0,
            )
        }
    }

    @Test
    fun `하위 카테고리는 parentId가 반드시 있어야 한다`() {
        shouldThrow<IllegalArgumentException> {
            Category(
                id = 5L,
                rootCategoryId = null,
                name = "셔츠",
                depth = 1,
                sortOrder = 0,
            )
        }
    }
}

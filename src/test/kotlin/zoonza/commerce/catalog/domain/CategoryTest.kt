package zoonza.commerce.catalog.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.domain.category.Category

class CategoryTest {
    @Test
    fun `루트 카테고리는 parentId가 없다`() {
        val category = Category(
            id = 1L,
            name = "  상의  ",
            parentId = null,
            depth = 0,
            sortOrder = 0,
        )

        category.name shouldBe "  상의  "
        category.parentId shouldBe null
        category.isRoot() shouldBe true
    }

    @Test
    fun `자식 카테고리는 루트가 아니다`() {
        val child = Category(
            id = 2L,
            parentId = 1L,
            name = "티셔츠",
            depth = 1,
            sortOrder = 0,
        )

        child.parentId shouldBe 1L
        child.depth shouldBe 1
        child.isRoot() shouldBe false
    }
}

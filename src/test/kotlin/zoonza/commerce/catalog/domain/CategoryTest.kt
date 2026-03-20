package zoonza.commerce.catalog.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CategoryTest {
    @Test
    fun `루트 카테고리를 생성하면 이름을 정규화한다`() {
        val category = Category.createRoot(name = "  상의  ", sortOrder = 0)

        category.name shouldBe "상의"
        category.isLeaf() shouldBe true
    }

    @Test
    fun `자식 카테고리를 생성하면 부모에 연결된다`() {
        val parent = Category.createRoot(name = "상의", sortOrder = 0)
        val child = Category.createChild(parent = parent, name = "티셔츠", sortOrder = 0)

        child.parent shouldBe parent
        parent.children shouldBe mutableListOf(child)
        parent.isLeaf() shouldBe false
    }

    @Test
    fun `자식 카테고리는 정렬 순서 기준으로 유지된다`() {
        val parent = Category.createRoot(name = "상의", sortOrder = 0)

        Category.createChild(parent = parent, name = "셔츠", sortOrder = 1)
        Category.createChild(parent = parent, name = "티셔츠", sortOrder = 0)

        parent.children.map { it.name } shouldBe listOf("티셔츠", "셔츠")
    }

    @Test
    fun `형제 카테고리의 정렬 순서는 중복될 수 없다`() {
        val parent = Category.createRoot(name = "상의", sortOrder = 0)
        Category.createChild(parent = parent, name = "티셔츠", sortOrder = 0)

        shouldThrow<IllegalArgumentException> {
            Category.createChild(parent = parent, name = "셔츠", sortOrder = 0)
        }
    }

    @Test
    fun `정렬 순서를 변경할 때도 형제와 중복되면 예외를 던진다`() {
        val parent = Category.createRoot(name = "상의", sortOrder = 0)
        Category.createChild(parent = parent, name = "티셔츠", sortOrder = 0)
        val second = Category.createChild(parent = parent, name = "셔츠", sortOrder = 1)

        shouldThrow<IllegalArgumentException> {
            second.changeSortOrder(0)
        }
    }
}

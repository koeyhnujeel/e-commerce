package zoonza.commerce.category

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CategoryTest {
    @Test
    fun `루트 카테고리를 생성한다`() {
        val category =
            Category.createRoot(
                name = "상의",
                sortOrder = 0,
            )

        category.name shouldBe "상의"
        category.sortOrder shouldBe 0
        category.parent shouldBe null
        category.isLeaf() shouldBe true
    }

    @Test
    fun `자식 카테고리를 생성하면 부모와 연결된다`() {
        val root =
            Category.createRoot(
                id = 1,
                name = "의류",
                sortOrder = 0,
            )

        val child =
            Category.createChild(
                parent = root,
                id = 2,
                name = "아우터",
                sortOrder = 1,
            )

        child.parent shouldBe root
        root.children shouldBe listOf(child)
        root.isLeaf() shouldBe false
        child.isLeaf() shouldBe true
    }

    @Test
    fun `카테고리명은 비어 있을 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Category.createRoot(
                    name = "   ",
                    sortOrder = 0,
                )
            }

        exception.message shouldBe "카테고리명은 비어 있을 수 없습니다."
    }

    @Test
    fun `카테고리 정렬 순서는 0 이상이어야 한다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Category.createRoot(
                    name = "상의",
                    sortOrder = -1,
                )
            }

        exception.message shouldBe "카테고리 정렬 순서는 0 이상이어야 합니다."
    }

    @Test
    fun `형제 카테고리의 정렬 순서는 중복될 수 없다`() {
        val root =
            Category.createRoot(
                name = "의류",
                sortOrder = 0,
            )
        Category.createChild(
            parent = root,
            name = "상의",
            sortOrder = 1,
        )

        val exception =
            shouldThrow<IllegalArgumentException> {
                Category.createChild(
                    parent = root,
                    name = "하의",
                    sortOrder = 1,
                )
            }

        exception.message shouldBe "형제 카테고리의 정렬 순서는 중복될 수 없습니다."
    }

    @Test
    fun `카테고리 정렬 순서 변경 시 형제와 중복되면 예외를 던진다`() {
        val root =
            Category.createRoot(
                name = "의류",
                sortOrder = 0,
            )
        Category.createChild(
            parent = root,
            name = "상의",
            sortOrder = 1,
        )
        val child =
            Category.createChild(
                parent = root,
                name = "하의",
                sortOrder = 2,
            )

        val exception =
            shouldThrow<IllegalArgumentException> {
                child.changeSortOrder(1)
            }

        exception.message shouldBe "형제 카테고리의 정렬 순서는 중복될 수 없습니다."
    }
}

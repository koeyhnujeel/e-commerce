package zoonza.commerce.adapter.out.persistence.category

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import zoonza.commerce.category.Category

class CategoryJpaEntityTest {
    @Test
    fun `도메인 카테고리를 JPA 엔티티로 변환한다`() {
        val root =
            Category.createRoot(
                id = 1L,
                name = "의류",
                sortOrder = 0,
            )
        Category.createChild(
            parent = root,
            id = 2L,
            name = "상의",
            sortOrder = 0,
        )
        Category.createChild(
            parent = root,
            id = 3L,
            name = "하의",
            sortOrder = 1,
        )

        val entity = CategoryJpaEntity.from(root)

        entity.id shouldBe 1L
        entity.name shouldBe "의류"
        entity.sortOrder shouldBe 0
        entity.parent shouldBe null
        entity.children.map { it.id } shouldContainExactly listOf(2L, 3L)
        entity.children.all { it.parent === entity } shouldBe true
    }

    @Test
    fun `JPA 엔티티 카테고리를 도메인 모델로 변환한다`() {
        val root =
            Category.createRoot(
                id = 1L,
                name = "의류",
                sortOrder = 0,
            )
        Category.createChild(
            parent = root,
            id = 2L,
            name = "상의",
            sortOrder = 0,
        )

        val entity = CategoryJpaEntity.from(root)

        val domain = entity.toDomain()

        domain.id shouldBe 1L
        domain.name shouldBe "의류"
        domain.children.map { it.id } shouldContainExactly listOf(2L)
        domain.children.first().parent shouldBe domain
    }
}

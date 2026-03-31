package zoonza.commerce.catalog.adapter.`in`

import io.kotest.assertions.throwables.shouldThrow
import jakarta.servlet.ServletException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.category.CategoryJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.category.CategoryJpaRepository
import zoonza.commerce.support.MySqlTestContainerConfig

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class CategoryControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var categoryJpaRepository: CategoryJpaRepository

    @Test
    fun `루트 카테고리 조회는 루트만 반환한다`() {
        val clothing = saveCategory(CategoryJpaEntity(name = "의류", depth = 0, sortOrder = 0))
        saveCategory(CategoryJpaEntity(name = "신발", depth = 0, sortOrder = 1))
        saveCategory(CategoryJpaEntity(name = "상의", rootCategoryId = clothing.id, depth = 1, sortOrder = 0))

        mockMvc
            .get("/api/categories/roots")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].name") { value("의류") }
                jsonPath("$.data[0].sub") { doesNotExist() }
                jsonPath("$.data[1].name") { value("신발") }
            }
    }

    @Test
    fun `전체 카테고리 조회는 root와 sub 2단계로 반환한다`() {
        val clothing = saveCategory(CategoryJpaEntity(name = "의류", depth = 0, sortOrder = 0))
        saveCategory(CategoryJpaEntity(name = "신발", depth = 0, sortOrder = 1))
        saveCategory(CategoryJpaEntity(name = "상의", rootCategoryId = clothing.id, depth = 1, sortOrder = 0))
        saveCategory(CategoryJpaEntity(name = "하의", rootCategoryId = clothing.id, depth = 1, sortOrder = 1))

        mockMvc
            .get("/api/categories")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].name") { value("의류") }
                jsonPath("$.data[0].sub[0].name") { value("상의") }
                jsonPath("$.data[0].sub[1].name") { value("하의") }
                jsonPath("$.data[0].sub[0].sub.length()") { value(0) }
                jsonPath("$.data[1].name") { value("신발") }
            }
    }

    @Test
    fun `루트 카테고리의 하위 카테고리 조회는 직계 하위만 반환한다`() {
        val clothing = saveCategory(CategoryJpaEntity(name = "의류", depth = 0, sortOrder = 0))
        saveCategory(CategoryJpaEntity(name = "상의", rootCategoryId = clothing.id, depth = 1, sortOrder = 0))
        saveCategory(CategoryJpaEntity(name = "하의", rootCategoryId = clothing.id, depth = 1, sortOrder = 1))

        mockMvc
            .get("/api/categories/${clothing.id}/sub-categories")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].name") { value("상의") }
                jsonPath("$.data[1].name") { value("하의") }
                jsonPath("$.data[0].sortOrder") { value(0) }
            }
    }

    @Test
    fun `하위 카테고리가 없는 루트 카테고리 조회는 빈 배열을 반환한다`() {
        val clothing = saveCategory(CategoryJpaEntity(name = "의류", depth = 0, sortOrder = 0))

        mockMvc
            .get("/api/categories/${clothing.id}/sub-categories")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(0) }
            }
    }

    @Test
    fun `sub 카테고리의 하위 카테고리 조회는 400을 반환한다`() {
        val clothing = saveCategory(CategoryJpaEntity(name = "의류", depth = 0, sortOrder = 0))
        val bottom = saveCategory(CategoryJpaEntity(name = "하의", rootCategoryId = clothing.id, depth = 1, sortOrder = 1))

        mockMvc
            .get("/api/categories/${bottom.id}/sub-categories")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.message") { value("루트 카테고리만 하위 카테고리를 조회할 수 있습니다.") }
            }
    }

    @Test
    fun `2단계를 넘는 카테고리 데이터가 있으면 전체 카테고리 조회는 예외가 전파된다`() {
        val clothing = saveCategory(CategoryJpaEntity(name = "의류", depth = 0, sortOrder = 0))
        val top = saveCategory(CategoryJpaEntity(name = "상의", rootCategoryId = clothing.id, depth = 1, sortOrder = 0))
        saveCategory(CategoryJpaEntity(name = "니트", rootCategoryId = top.id, depth = 2, sortOrder = 0))

        shouldThrow<ServletException> {
            mockMvc.get("/api/categories")
        }
    }

    @Test
    fun `없는 카테고리의 하위 카테고리 조회는 404를 반환한다`() {
        mockMvc
            .get("/api/categories/999/sub-categories")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.message") { value("카테고리를 찾을 수 없습니다.") }
            }
    }

    private fun saveCategory(category: CategoryJpaEntity): CategoryJpaEntity {
        return categoryJpaRepository.save(category)
    }
}

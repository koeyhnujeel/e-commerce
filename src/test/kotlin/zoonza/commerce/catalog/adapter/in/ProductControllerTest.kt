package zoonza.commerce.catalog.adapter.`in`

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.category.CategoryJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.category.CategoryJpaRepository
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.catalog.adapter.out.persistence.statistic.ProductStatisticJpaRepository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.like.adapter.out.persistence.MemberLikeJpaRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.ProductFixture

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class ProductControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var categoryJpaRepository: CategoryJpaRepository

    @Autowired
    private lateinit var productStatisticJpaRepository: ProductStatisticJpaRepository

    @Autowired
    private lateinit var memberLikeJpaRepository: MemberLikeJpaRepository

    @Test
    fun `비로그인 사용자는 카테고리 필터와 가격 정렬로 상품 목록을 조회할 수 있다`() {
        val savedRootCategory = saveCategory(
            CategoryJpaEntity(
                name = "상의",
                parentId = null,
                depth = 0,
                sortOrder = 0,
            ),
        )
        val savedChildCategory = saveCategory(
            CategoryJpaEntity(
                parentId = savedRootCategory.id,
                name = "티셔츠",
                depth = savedRootCategory.depth + 1,
                sortOrder = 0,
            ),
        )

        val cheapProduct = productJpaRepository.save(
            ProductFixture.createCatalogProduct(
                index = 1,
                price = 19_900,
                categoryId = savedChildCategory.id,
            ),
        )
        val expensiveProduct = productJpaRepository.save(
            ProductFixture.createCatalogProduct(
                index = 2,
                price = 39_900,
                categoryId = savedRootCategory.id,
            ),
        )
        val otherCategory = saveCategory(CategoryJpaEntity(name = "하의", depth = 0, sortOrder = 1))
        productJpaRepository.save(
            ProductFixture.createCatalogProduct(
                index = 3,
                price = 9_900,
                categoryId = otherCategory.id,
            ),
        )

        productStatisticJpaRepository.save(ProductStatistic.create(productId = cheapProduct.id, likeCount = 1L))

        mockMvc
            .get("/api/products") {
                param("categoryId", savedRootCategory.id.toString())
                param("sort", "PRICE_DESC")
                param("page", "1")
                param("size", "10")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.page") { value(1) }
                jsonPath("$.data.items.length()") { value(2) }
                jsonPath("$.data.items[0].productId") { value(expensiveProduct.id) }
                jsonPath("$.data.items[0].likedByMe") { value(false) }
                jsonPath("$.data.items[1].productId") { value(cheapProduct.id) }
                jsonPath("$.data.items[1].likeCount") { value(1) }
                jsonPath("$.data.items[1].saleStatus") { value("AVAILABLE") }
            }
    }

    @Test
    fun `로그인 사용자는 상품 목록에서 likedByMe를 확인할 수 있다`() {
        val category = saveCategory(CategoryJpaEntity(name = "상의", depth = 0, sortOrder = 0))
        val likedProduct = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryId = category.id))
        val unlikedProduct = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 2, price = 29_900, categoryId = category.id))
        productStatisticJpaRepository.save(ProductStatistic.create(productId = likedProduct.id, likeCount = 2L))
        productStatisticJpaRepository.save(ProductStatistic.create(productId = unlikedProduct.id, likeCount = 1L))

        memberLikeJpaRepository.save(MemberLike.create(memberId = 1L, targetId = likedProduct.id, targetType = LikeTargetType.PRODUCT))
        memberLikeJpaRepository.save(MemberLike.create(memberId = 2L, targetId = likedProduct.id, targetType = LikeTargetType.PRODUCT))
        memberLikeJpaRepository.save(MemberLike.create(memberId = 2L, targetId = unlikedProduct.id, targetType = LikeTargetType.PRODUCT))

        mockMvc
            .get("/api/products") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                param("categoryId", category.id.toString())
                param("sort", "LATEST")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.items[0].productId") { value(unlikedProduct.id) }
                jsonPath("$.data.items[0].likedByMe") { value(false) }
                jsonPath("$.data.items[1].productId") { value(likedProduct.id) }
                jsonPath("$.data.items[1].likedByMe") { value(true) }
                jsonPath("$.data.items[1].likeCount") { value(2) }
            }
    }

    @Test
    fun `비로그인 사용자는 상품 상세를 조회할 수 있다`() {
        val category = saveCategory(CategoryJpaEntity(name = "셔츠", depth = 0, sortOrder = 0))
        val product = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryId = category.id))
        productStatisticJpaRepository.save(ProductStatistic.create(productId = product.id, likeCount = 1L))

        mockMvc
            .get("/api/products/${product.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.productId") { value(product.id) }
                jsonPath("$.data.categoryId") { value(category.id) }
                jsonPath("$.data.images.length()") { value(2) }
                jsonPath("$.data.options.length()") { value(2) }
                jsonPath("$.data.options[0].sortOrder") { value(0) }
                jsonPath("$.data.options[1].additionalPrice") { value(1000) }
                jsonPath("$.data.likeCount") { value(1) }
                jsonPath("$.data.likedByMe") { value(false) }
                jsonPath("$.data.saleStatus") { value("AVAILABLE") }
            }
    }

    @Test
    fun `로그인 사용자는 상품 상세에서 likedByMe를 확인할 수 있다`() {
        val category = saveCategory(CategoryJpaEntity(name = "바지", depth = 0, sortOrder = 0))
        val product = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryId = category.id))
        productStatisticJpaRepository.save(ProductStatistic.create(productId = product.id, likeCount = 1L))

        memberLikeJpaRepository.save(MemberLike.create(memberId = 7L, targetId = product.id, targetType = LikeTargetType.PRODUCT))

        mockMvc
            .get("/api/products/${product.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 7L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.productId") { value(product.id) }
                jsonPath("$.data.likedByMe") { value(true) }
                jsonPath("$.data.likeCount") { value(1) }
            }
    }

    @Test
    fun `통계 row가 없으면 상품 상세의 likeCount는 0이다`() {
        val category = saveCategory(CategoryJpaEntity(name = "아우터", depth = 0, sortOrder = 0))
        val product = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryId = category.id))

        mockMvc
            .get("/api/products/${product.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.productId") { value(product.id) }
                jsonPath("$.data.likeCount") { value(0) }
            }
    }

    @Test
    fun `통계 row가 없으면 상품 목록의 likeCount는 0이다`() {
        val category = saveCategory(CategoryJpaEntity(name = "상의", depth = 0, sortOrder = 0))
        val product = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryId = category.id))

        mockMvc
            .get("/api/products") {
                param("categoryId", category.id.toString())
            }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.items[0].productId") { value(product.id) }
                jsonPath("$.data.items[0].likeCount") { value(0) }
            }
    }

    @Test
    fun `카테고리 ID 없이 상품 목록을 조회할 수 없다`() {
        mockMvc
            .get("/api/products")
            .andExpect {
                status { isBadRequest() }
            }
    }

    private fun saveCategory(category: CategoryJpaEntity): CategoryJpaEntity {
        return categoryJpaRepository.save(category)
    }
}

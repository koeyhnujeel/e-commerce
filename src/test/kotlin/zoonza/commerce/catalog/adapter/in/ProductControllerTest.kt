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
import zoonza.commerce.catalog.adapter.out.persistence.ProductJpaRepository
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
    private lateinit var memberLikeJpaRepository: MemberLikeJpaRepository

    @Test
    fun `비로그인 사용자는 카테고리 필터와 가격 정렬로 상품 목록을 조회할 수 있다`() {
        val cheapProduct = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryIds = listOf(1L)))
        val expensiveProduct = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 2, price = 39_900, categoryIds = listOf(1L)))
        productJpaRepository.save(ProductFixture.createCatalogProduct(index = 3, price = 9_900, categoryIds = listOf(2L)))

        memberLikeJpaRepository.save(MemberLike.create(memberId = 11L, targetId = cheapProduct.id, targetType = LikeTargetType.PRODUCT))
        memberLikeJpaRepository.save(MemberLike.create(memberId = 12L, targetId = cheapProduct.id, targetType = LikeTargetType.PRODUCT).apply { cancel() })

        mockMvc
            .get("/api/products") {
                param("categoryId", "1")
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
        val likedProduct = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryIds = listOf(1L)))
        val unlikedProduct = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 2, price = 29_900, categoryIds = listOf(1L)))

        memberLikeJpaRepository.save(MemberLike.create(memberId = 1L, targetId = likedProduct.id, targetType = LikeTargetType.PRODUCT))
        memberLikeJpaRepository.save(MemberLike.create(memberId = 2L, targetId = likedProduct.id, targetType = LikeTargetType.PRODUCT))
        memberLikeJpaRepository.save(MemberLike.create(memberId = 2L, targetId = unlikedProduct.id, targetType = LikeTargetType.PRODUCT))

        mockMvc
            .get("/api/products") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
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
        val product = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryIds = listOf(20L, 10L)))

        memberLikeJpaRepository.save(MemberLike.create(memberId = 2L, targetId = product.id, targetType = LikeTargetType.PRODUCT))

        mockMvc
            .get("/api/products/${product.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.productId") { value(product.id) }
                jsonPath("$.data.categoryIds[0]") { value(10) }
                jsonPath("$.data.images.length()") { value(2) }
                jsonPath("$.data.options.length()") { value(2) }
                jsonPath("$.data.options[0].orderable") { value(true) }
                jsonPath("$.data.likeCount") { value(1) }
                jsonPath("$.data.likedByMe") { value(false) }
                jsonPath("$.data.saleStatus") { value("AVAILABLE") }
            }
    }

    @Test
    fun `로그인 사용자는 상품 상세에서 likedByMe를 확인할 수 있다`() {
        val product = productJpaRepository.save(ProductFixture.createCatalogProduct(index = 1, price = 19_900, categoryIds = listOf(1L)))

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
}

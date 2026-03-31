package zoonza.commerce.cart.adapter.`in`

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.*
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.cart.adapter.`in`.request.AddCartItemRequest
import zoonza.commerce.cart.adapter.`in`.request.ChangeCartItemQuantityRequest
import zoonza.commerce.cart.adapter.out.persistence.CartJpaRepository
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaEntity
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaRepository
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.ProductFixture

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class CartControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var stockJpaRepository: StockJpaRepository

    @Autowired
    private lateinit var cartJpaRepository: CartJpaRepository

    @Test
    fun `인증 정보가 없으면 장바구니 조회는 401 응답을 반환한다`() {
        mockMvc
            .get("/api/cart")
            .andExpect {
                status { isUnauthorized() }
                header { string(HttpHeaders.WWW_AUTHENTICATE, "Bearer") }
                jsonPath("$.success") { value(false) }
            }
    }

    @Test
    fun `인증된 회원은 장바구니에 상품을 담고 조회할 수 있다`() {
        val savedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 1, price = 10_000, additionalPrice = 1_000))
        val optionId = savedProduct.options.single().id
        stockJpaRepository.save(StockJpaEntity.from(Stock.create(productOptionId = optionId, totalQuantity = 10L)))

        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AddCartItemRequest(productId = savedProduct.id, productOptionId = optionId, quantity = 2L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        mockMvc
            .get("/api/cart") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items[0].productId") { value(savedProduct.id) }
                jsonPath("$.data.items[0].productOptionId") { value(optionId) }
                jsonPath("$.data.items[0].quantity") { value(2) }
                jsonPath("$.data.items[0].primaryImageUrl") { value("https://cdn.example.com/product-1-primary.jpg") }
                jsonPath("$.data.items[0].unitPrice") { value(11_000) }
                jsonPath("$.data.items[0].lineAmount") { value(22_000) }
                jsonPath("$.data.items[0].purchasable") { value(true) }
                jsonPath("$.data.summary.itemCount") { value(1) }
                jsonPath("$.data.summary.totalQuantity") { value(2) }
                jsonPath("$.data.summary.totalAmount") { value(22_000) }
                jsonPath("$.data.summary.purchasableItemCount") { value(1) }
                jsonPath("$.data.summary.purchasableAmount") { value(22_000) }
            }
    }

    @Test
    fun `대표 이미지가 없어도 장바구니 담기와 조회가 가능하다`() {
        val savedProduct =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 4,
                    primaryImage = false,
                ),
            )
        val optionId = savedProduct.options.single().id

        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AddCartItemRequest(productId = savedProduct.id, productOptionId = optionId, quantity = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        mockMvc
            .get("/api/cart") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.items[0].primaryImageUrl") { value("https://cdn.example.com/product-4-primary.jpg") }
            }
    }

    @Test
    fun `다른 상품의 옵션을 함께 보내면 장바구니에 담을 수 없다`() {
        val firstProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 6))
        val secondProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 7))
        val secondOptionId = secondProduct.options.single().id

        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    AddCartItemRequest(
                        productId = firstProduct.id,
                        productOptionId = secondOptionId,
                        quantity = 1L,
                    ),
                )
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.message") { value("상품 옵션을 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `재고가 없으면 장바구니 조회에서 구매 불가 상태를 반환한다`() {
        val savedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 2, price = 20_000))
        val optionId = savedProduct.options.single().id

        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AddCartItemRequest(productId = savedProduct.id, productOptionId = optionId, quantity = 1L))
            }.andExpect {
                status { isOk() }
            }

        mockMvc
            .get("/api/cart") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.items[0].purchasable") { value(false) }
                jsonPath("$.data.items[0].unavailableReason") { value("OUT_OF_STOCK") }
                jsonPath("$.data.summary.purchasableItemCount") { value(0) }
            }
    }

    @Test
    fun `인증된 회원은 장바구니 수량을 변경하고 비울 수 있다`() {
        val savedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 3))
        val optionId = savedProduct.options.single().id
        stockJpaRepository.save(StockJpaEntity.from(Stock.create(productOptionId = optionId, totalQuantity = 10L)))

        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AddCartItemRequest(productId = savedProduct.id, productOptionId = optionId, quantity = 1L))
            }

        mockMvc
            .patch("/api/cart/items/$optionId") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(ChangeCartItemQuantityRequest(quantity = 4L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        mockMvc
            .delete("/api/cart") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val cart = cartJpaRepository.findByMemberId(1L)
        org.assertj.core.api.Assertions.assertThat(cart).isNotNull
        org.assertj.core.api.Assertions.assertThat(cart?.items).isEmpty()
    }

    @Test
    fun `판매 중지 상품은 장바구니에 담을 수 없다`() {
        val savedProduct =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 5,
                    saleStatus = ProductSaleStatus.UNAVAILABLE,
                ),
            )
        val optionId = savedProduct.options.single().id

        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AddCartItemRequest(productId = savedProduct.id, productOptionId = optionId, quantity = 1L))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.message") { value("구매할 수 없는 상품입니다.") }
            }
    }

    @Test
    fun `장바구니 요청 본문이 잘못되면 400 응답을 반환한다`() {
        mockMvc
            .post("/api/cart/items") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(AddCartItemRequest(productId = 0L, productOptionId = 0L, quantity = 0L))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }
    }
}

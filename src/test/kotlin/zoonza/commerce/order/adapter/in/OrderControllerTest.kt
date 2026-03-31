package zoonza.commerce.order.adapter.`in`

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaEntity
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaRepository
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.member.domain.MemberAddress
import zoonza.commerce.order.adapter.`in`.request.PlaceDirectOrderRequest
import zoonza.commerce.order.adapter.out.persistence.OrderJpaRepository
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.MemberFixture
import zoonza.commerce.support.fixture.ProductFixture

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class OrderControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider

    @Autowired
    private lateinit var memberJapRepository: MemberJapRepository

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var stockJpaRepository: StockJpaRepository

    @Autowired
    private lateinit var orderJpaRepository: OrderJpaRepository

    @Test
    fun `인증된 회원은 바로구매 주문을 생성할 수 있다`() {
        val savedMember = createMemberWithAddress()
        val savedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 11, price = 20_000, additionalPrice = 1_000))
        val optionId = savedProduct.options.single().id
        val addressId = savedMember.addresses.single().id
        stockJpaRepository.save(StockJpaEntity.from(Stock.create(productOptionId = optionId, totalQuantity = 10L)))

        mockMvc
            .post("/api/orders/direct") {
                header("Authorization", AuthFixture.authorizationHeader(accessTokenProvider, savedMember.id, savedMember.email))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    PlaceDirectOrderRequest(
                        productId = savedProduct.id,
                        productOptionId = optionId,
                        quantity = 2L,
                        addressId = addressId,
                    ),
                )
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.totalAmount") { value(42_000) }
                jsonPath("$.data.orderNumber") { exists() }
            }

        val order = orderJpaRepository.findAllByMemberIdOrderByOrderedAtDesc(savedMember.id).single()
        order.status shouldBe OrderStatus.PENDING_PAYMENT
        order.recipient.baseAddress shouldBe "서울시 강남구 테헤란로 1"
        order.items.single().quantity shouldBe 2L
    }

    @Test
    fun `결제 대기 주문은 취소하면 재고 예약을 해제한다`() {
        val savedMember = createMemberWithAddress()
        val savedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 12))
        val optionId = savedProduct.options.single().id
        val addressId = savedMember.addresses.single().id
        stockJpaRepository.save(StockJpaEntity.from(Stock.create(productOptionId = optionId, totalQuantity = 10L)))

        mockMvc
            .post("/api/orders/direct") {
                header("Authorization", AuthFixture.authorizationHeader(accessTokenProvider, savedMember.id, savedMember.email))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    PlaceDirectOrderRequest(
                        productId = savedProduct.id,
                        productOptionId = optionId,
                        quantity = 1L,
                        addressId = addressId,
                    ),
                )
            }

        val order = orderJpaRepository.findAllByMemberIdOrderByOrderedAtDesc(savedMember.id).single()

        mockMvc
            .post("/api/orders/${order.id}/cancel") {
                header("Authorization", AuthFixture.authorizationHeader(accessTokenProvider, savedMember.id, savedMember.email))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val canceledOrder = orderJpaRepository.findByIdAndMemberId(order.id, savedMember.id)!!
        val stock = stockJpaRepository.findByProductOptionId(optionId)!!
        canceledOrder.status shouldBe OrderStatus.CANCELED
        stock.reservedQuantity shouldBe 0L
    }

    private fun createMemberWithAddress() =
        memberJapRepository.save(
            MemberFixture.createJpa(
                email = "order-member@example.com",
                name = "주문회원",
                nickname = "order-member",
                phoneNumber = "01055556666",
                addresses =
                    mutableListOf(
                        MemberAddress.create(
                            label = "집",
                            recipientName = "주문회원",
                            recipientPhoneNumber = "01055556666",
                            zipCode = "06236",
                            baseAddress = "서울시 강남구 테헤란로 1",
                            detailAddress = "",
                            isDefault = true,
                        ),
                    ),
            ),
        )
}

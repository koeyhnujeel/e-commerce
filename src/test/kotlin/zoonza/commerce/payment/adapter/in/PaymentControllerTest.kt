package zoonza.commerce.payment.adapter.`in`

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaEntity
import zoonza.commerce.inventory.adapter.out.persistence.StockJpaRepository
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.member.domain.MemberAddress
import zoonza.commerce.order.adapter.`in`.request.PlaceDirectOrderRequest
import zoonza.commerce.order.adapter.out.persistence.OrderJpaRepository
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.payment.adapter.out.persistence.PaymentJpaRepository
import zoonza.commerce.payment.application.port.out.PaymentGatewayCancellation
import zoonza.commerce.payment.application.port.out.PaymentGatewayConfirmation
import zoonza.commerce.payment.application.port.out.PaymentGatewayLookup
import zoonza.commerce.payment.application.port.out.PaymentGatewayPort
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.MemberFixture
import zoonza.commerce.support.fixture.ProductFixture
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class, PaymentControllerTest.FakePaymentGatewayConfig::class)
class PaymentControllerTest {
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

    @Autowired
    private lateinit var paymentJpaRepository: PaymentJpaRepository

    @Autowired
    private lateinit var fakePaymentGatewayPort: FakePaymentGatewayPort

    @Test
    fun `인증된 회원은 결제 준비 정보를 조회할 수 있다`() {
        val savedMember = createMemberWithAddress()
        val order = createPendingOrder(savedMember.id, savedMember.email)

        val response =
            mockMvc
                .post("/api/payments/orders/${order.id}/prepare") {
                    header("Authorization", AuthFixture.authorizationHeader(accessTokenProvider, savedMember.id, savedMember.email))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.provider") { value("TOSS_PAYMENTS") }
                    jsonPath("$.data.clientKey") { value("test-client-key") }
                }.andReturn()

        val payload = objectMapper.readTree(response.response.contentAsString)
        val payment = paymentJpaRepository.findByOrderId(order.id)!!
        val providerOrderId = payload["data"]["providerOrderId"].asText()

        providerOrderId shouldBe "${order.orderNumber}-1"
        payment.attempts.single().providerOrderId shouldBe providerOrderId
    }

    @Test
    fun `성공 콜백은 결제를 승인하고 프론트 성공 URL로 리다이렉트한다`() {
        val savedMember = createMemberWithAddress()
        val order = createPendingOrder(savedMember.id, savedMember.email)
        val prepareResponse = preparePayment(savedMember.id, savedMember.email, order.id)
        val data = objectMapper.readTree(prepareResponse.response.contentAsString)["data"]
        val token = UriComponentsBuilder.fromUriString(data["successUrl"].asText()).build().queryParams.getFirst("token")!!
        val providerOrderId = data["providerOrderId"].asText()
        val amount = data["amount"].asLong()
        val paymentId = paymentJpaRepository.findByOrderId(order.id)!!.id

        fakePaymentGatewayPort.confirmation =
            PaymentGatewayConfirmation(
                paymentKey = "payment-key",
                providerOrderId = providerOrderId,
                totalAmount = amount,
                method = "CARD",
                approvedAt = LocalDateTime.of(2026, 4, 1, 10, 5),
            )

        val result =
            mockMvc
                .get("/api/payments/toss/success") {
                    queryParam("token", token)
                    queryParam("paymentKey", "payment-key")
                    queryParam("orderId", providerOrderId)
                    queryParam("amount", amount.toString())
                }.andExpect {
                    status { isSeeOther() }
                }.andReturn()

        result.response.getHeader("Location") shouldBe "https://example.com/payments/success?orderId=${order.id}&paymentId=$paymentId"
        orderJpaRepository.findByIdAndMemberId(order.id, savedMember.id)!!.status shouldBe OrderStatus.PAID
        paymentJpaRepository.findByOrderId(order.id)!!.status.name shouldBe "APPROVED"
    }

    @Test
    fun `웹훅은 DONE 상태를 승인 처리한다`() {
        val savedMember = createMemberWithAddress()
        val order = createPendingOrder(savedMember.id, savedMember.email)
        val prepareResponse = preparePayment(savedMember.id, savedMember.email, order.id)
        val data = objectMapper.readTree(prepareResponse.response.contentAsString)["data"]
        val providerOrderId = data["providerOrderId"].asText()
        val amount = data["amount"].asLong()

        fakePaymentGatewayPort.lookup =
            PaymentGatewayLookup(
                paymentKey = "payment-key",
                providerOrderId = providerOrderId,
                status = "DONE",
                totalAmount = amount,
                method = "CARD",
                approvedAt = LocalDateTime.of(2026, 4, 1, 10, 5),
            )

        mockMvc
            .post("/api/payments/toss/webhooks") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "eventType" to "PAYMENT_STATUS_CHANGED",
                            "data" to mapOf("paymentKey" to "payment-key", "orderId" to providerOrderId),
                        ),
                    )
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        orderJpaRepository.findByIdAndMemberId(order.id, savedMember.id)!!.status shouldBe OrderStatus.PAID
    }

    private fun preparePayment(
        memberId: Long,
        email: String,
        orderId: Long,
    ): MvcResult {
        return mockMvc
            .post("/api/payments/orders/$orderId/prepare") {
                header("Authorization", AuthFixture.authorizationHeader(accessTokenProvider, memberId, email))
            }.andReturn()
    }

    private fun createPendingOrder(
        memberId: Long,
        email: String,
    ): zoonza.commerce.order.adapter.out.persistence.OrderJpaEntity {
        val savedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 21, price = 20_000, additionalPrice = 1_000))
        val optionId = savedProduct.options.single().id
        val addressId = memberJapRepository.findById(memberId).orElseThrow().addresses.single().id
        stockJpaRepository.save(StockJpaEntity.from(Stock.create(productOptionId = optionId, totalQuantity = 10L)))

        mockMvc
            .post("/api/orders/direct") {
                header("Authorization", AuthFixture.authorizationHeader(accessTokenProvider, memberId, email))
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
            }

        return orderJpaRepository.findAllByMemberIdOrderByOrderedAtDesc(memberId).single()
    }

    private fun createMemberWithAddress() =
        memberJapRepository.save(
            MemberFixture.createJpa(
                email = "payment-member@example.com",
                name = "결제회원",
                nickname = "payment-member",
                phoneNumber = "01099990000",
                addresses =
                    mutableListOf(
                        MemberAddress.create(
                            label = "집",
                            recipientName = "결제회원",
                            recipientPhoneNumber = "01099990000",
                            zipCode = "06236",
                            baseAddress = "서울시 강남구 테헤란로 1",
                            detailAddress = "",
                            isDefault = true,
                        ),
                    ),
            ),
        )

    @TestConfiguration
    class FakePaymentGatewayConfig {
        @Bean
        @Primary
        fun fakePaymentGatewayPort(): FakePaymentGatewayPort = FakePaymentGatewayPort()
    }

    class FakePaymentGatewayPort : PaymentGatewayPort {
        var confirmation: PaymentGatewayConfirmation? = null
        var lookup: PaymentGatewayLookup? = null
        var cancellation: PaymentGatewayCancellation? = null

        override fun confirm(
            paymentKey: String,
            providerOrderId: String,
            amount: Long,
            idempotencyKey: String,
        ): PaymentGatewayConfirmation {
            return confirmation ?: error("confirmation result is not configured")
        }

        override fun cancel(
            paymentKey: String,
            cancelReason: String,
            amount: Long,
            idempotencyKey: String,
        ): PaymentGatewayCancellation {
            return cancellation ?: error("cancellation result is not configured")
        }

        override fun get(paymentKey: String): PaymentGatewayLookup {
            return lookup ?: error("lookup result is not configured")
        }
    }
}

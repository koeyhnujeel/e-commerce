package zoonza.commerce.payment.adapter.`in`

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.order.adapter.out.persistence.OrderJpaRepository
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.payment.adapter.out.persistence.PaymentJpaRepository
import zoonza.commerce.payment.application.port.out.*
import zoonza.commerce.payment.domain.PaymentStatus
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.MemberFixture
import zoonza.commerce.support.fixture.OrderFixture
import zoonza.commerce.support.fixture.ProductFixture
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class PaymentControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider

    @Autowired
    private lateinit var memberJapRepository: MemberJapRepository

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var orderJpaRepository: OrderJpaRepository

    @Autowired
    private lateinit var paymentJpaRepository: PaymentJpaRepository

    @MockBean
    private lateinit var paymentGatewayClient: PaymentGatewayClient

    @Test
    fun `인증된 회원은 주문으로 결제를 생성할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "payment-member",
                    nicknamePrefix = "payment-nickname",
                    phoneNumberPrefix = "0102000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "결제상품",
                    descriptionPrefix = "결제상품 설명",
                    imagePrefix = "payment-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-PAYMENT-1",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .post("/api/orders/${order.id}/payments") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "amount": 19900,
                      "paymentMethod": "CARD"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.orderId") { value(order.id) }
                jsonPath("$.data.orderNumber") { value("ORD-PAYMENT-1") }
                jsonPath("$.data.status") { value("READY") }
                jsonPath("$.data.checkout.orderId") { value("ORD-PAYMENT-1") }
                jsonPath("$.data.checkout.orderName") { value("결제상품1") }
                jsonPath("$.data.checkout.customerKey") { value("member-${member.id}") }
            }

        val savedPayment = paymentJpaRepository.findAll().single()
        savedPayment.orderId shouldBe order.id
        savedPayment.status shouldBe PaymentStatus.READY
        savedPayment.isActive() shouldBe true

        val updatedOrder = orderJpaRepository.findById(order.id).orElseThrow()
        updatedOrder.status shouldBe OrderStatus.PAYMENT_PENDING
    }

    @Test
    fun `인증된 회원은 자신의 결제 상세를 조회할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "payment-member",
                    nicknamePrefix = "payment-nickname",
                    phoneNumberPrefix = "0102000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "결제상품",
                    descriptionPrefix = "결제상품 설명",
                    imagePrefix = "payment-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-PAYMENT-2",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .post("/api/orders/${order.id}/payments") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "amount": 19900,
                      "paymentMethod": "CARD"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
            }

        val payment = paymentJpaRepository.findAll().single()

        mockMvc
            .get("/api/payments/${payment.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.paymentId") { value(payment.id) }
                jsonPath("$.data.orderId") { value(order.id) }
                jsonPath("$.data.status") { value("READY") }
                jsonPath("$.data.providerReference") { value("ORD-PAYMENT-2") }
            }
    }

    @Test
    fun `타인 결제 상세는 조회할 수 없다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "payment-member",
                    nicknamePrefix = "payment-nickname",
                    phoneNumberPrefix = "0102000000",
                ),
            )
        val otherMember =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 2,
                    emailPrefix = "payment-member",
                    nicknamePrefix = "payment-nickname",
                    phoneNumberPrefix = "0102000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "결제상품",
                    descriptionPrefix = "결제상품 설명",
                    imagePrefix = "payment-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = otherMember.id,
                    product = product,
                    orderNumber = "ORD-PAYMENT-3",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .post("/api/orders/${order.id}/payments") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = otherMember.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "amount": 19900,
                      "paymentMethod": "CARD"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
            }

        val payment = paymentJpaRepository.findAll().single()

        mockMvc
            .get("/api/payments/${payment.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.error.code") { value("NOT_FOUND") }
            }
    }

    @Test
    fun `인증된 회원은 토스 승인 결과로 결제를 확정할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "payment-member",
                    nicknamePrefix = "payment-nickname",
                    phoneNumberPrefix = "0102000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "결제상품",
                    descriptionPrefix = "결제상품 설명",
                    imagePrefix = "payment-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-PAYMENT-4",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .post("/api/orders/${order.id}/payments") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content = """{"amount": 19900, "paymentMethod": "CARD"}"""
            }.andExpect {
                status { isOk() }
            }

        val payment = paymentJpaRepository.findAll().single()
        given(
            paymentGatewayClient.confirm(
                PaymentConfirmRequest(
                    paymentKey = "pay_123",
                    orderId = "ORD-PAYMENT-4",
                    amount = 19_900,
                ),
            ),
        ).willReturn(
            PaymentConfirmResult(
                paymentKey = "pay_123",
                method = "CARD",
                providerReference = "tx_123",
                approvedAt = LocalDateTime.of(2026, 3, 22, 12, 5),
            ),
        )

        mockMvc
            .post("/api/payments/${payment.id}/confirm") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "paymentKey": "pay_123",
                      "orderId": "ORD-PAYMENT-4",
                      "amount": 19900
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.status") { value("CONFIRMED") }
                jsonPath("$.data.paymentKey") { value("pay_123") }
                jsonPath("$.data.providerReference") { value("tx_123") }
            }

        val confirmedPayment = paymentJpaRepository.findById(payment.id).orElseThrow()
        confirmedPayment.status shouldBe PaymentStatus.CONFIRMED
        val confirmedOrder = orderJpaRepository.findById(order.id).orElseThrow()
        confirmedOrder.status shouldBe OrderStatus.PAID
    }

    @Test
    fun `인증된 회원은 결제를 취소할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "payment-member",
                    nicknamePrefix = "payment-nickname",
                    phoneNumberPrefix = "0102000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "결제상품",
                    descriptionPrefix = "결제상품 설명",
                    imagePrefix = "payment-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-PAYMENT-5",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .post("/api/orders/${order.id}/payments") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content = """{"amount": 19900, "paymentMethod": "CARD"}"""
            }.andExpect {
                status { isOk() }
            }

        val payment = paymentJpaRepository.findAll().single()
        given(
            paymentGatewayClient.confirm(
                PaymentConfirmRequest(
                    paymentKey = "pay_456",
                    orderId = "ORD-PAYMENT-5",
                    amount = 19_900,
                ),
            ),
        ).willReturn(
            PaymentConfirmResult(
                paymentKey = "pay_456",
                method = "CARD",
                providerReference = "tx_456",
                approvedAt = LocalDateTime.of(2026, 3, 22, 12, 5),
            ),
        )

        mockMvc
            .post("/api/payments/${payment.id}/confirm") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "paymentKey": "pay_456",
                      "orderId": "ORD-PAYMENT-5",
                      "amount": 19900
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
            }

        given(
            paymentGatewayClient.cancel(
                paymentKey = "pay_456",
                request = PaymentCancelRequest(cancelReason = "고객 요청"),
            ),
        ).willReturn(
            PaymentCancelResult(
                providerReference = "cancel_tx_456",
                cancelReason = "고객 요청",
                canceledAt = LocalDateTime.of(2026, 3, 22, 12, 10),
            ),
        )

        mockMvc
            .post("/api/payments/${payment.id}/cancel") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content = """{"reason": "고객 요청"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.status") { value("CANCELED") }
                jsonPath("$.data.failureReason") { value("고객 요청") }
            }

        val canceledPayment = paymentJpaRepository.findById(payment.id).orElseThrow()
        canceledPayment.status shouldBe PaymentStatus.CANCELED
        val canceledOrder = orderJpaRepository.findById(order.id).orElseThrow()
        canceledOrder.status shouldBe OrderStatus.CANCELED
    }
}

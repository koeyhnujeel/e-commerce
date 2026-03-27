package zoonza.commerce.order.adapter.`in`

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.order.adapter.out.persistence.OrderJpaRepository
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.order.domain.OrderStatus
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
class OrderControllerTest {
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

    @Test
    fun `인증된 회원은 주문을 생성할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val productOption = product.options.single()

        mockMvc
            .post("/api/orders") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "items": [
                        {
                          "productId": ${product.id},
                          "productOptionId": ${productOption.id},
                          "quantity": 2
                        }
                      ]
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.orderId") { exists() }
                jsonPath("$.data.orderNumber") { exists() }
                jsonPath("$.data.totalAmount") { value(39_800) }
            }

        val savedOrder = orderJpaRepository.findAll().single()
        savedOrder.memberId shouldBe member.id
        savedOrder.status shouldBe OrderStatus.CREATED
        savedOrder.totalAmount.amount shouldBe 39_800
        savedOrder.items.single().productNameSnapshot shouldBe "주문상품1"
        savedOrder.items.single().optionColorSnapshot shouldBe "BLACK"
        savedOrder.items.single().optionSizeSnapshot shouldBe "M"
    }

    @Test
    fun `인증된 회원은 자신의 주문 목록을 최신순으로 조회할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val otherMember =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 2,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )

        orderJpaRepository.save(
            OrderFixture.create(
                memberId = member.id,
                product = product,
                orderNumber = "ORD-OLD",
                orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            ),
        )
        orderJpaRepository.save(
            OrderFixture.create(
                memberId = member.id,
                product = product,
                orderNumber = "ORD-NEW",
                orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
            ),
        )
        orderJpaRepository.save(
            OrderFixture.create(
                memberId = otherMember.id,
                product = product,
                orderNumber = "ORD-OTHER",
                orderedAt = LocalDateTime.of(2026, 3, 23, 10, 0),
            ),
        )

        mockMvc
            .get("/api/orders") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].orderNumber") { value("ORD-NEW") }
                jsonPath("$.data[1].orderNumber") { value("ORD-OLD") }
            }
    }

    @Test
    fun `인증된 회원은 자신의 주문 상세를 조회할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-DETAIL",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .get("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.orderId") { value(order.id) }
                jsonPath("$.data.orderNumber") { value("ORD-DETAIL") }
                jsonPath("$.data.items.length()") { value(1) }
                jsonPath("$.data.items[0].productName") { value("주문상품1") }
                jsonPath("$.data.items[0].lineAmount") { value(19_900) }
            }
    }

    @Test
    fun `인증된 회원은 결제 전 주문을 수정할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val anotherProduct =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 2,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-UPDATE",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )
        val anotherOption = anotherProduct.options.single()

        mockMvc
            .patch("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "items": [
                        {
                          "productId": ${anotherProduct.id},
                          "productOptionId": ${anotherOption.id},
                          "quantity": 2
                        }
                      ]
                    }
                    """.trimIndent()
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.orderId") { value(order.id) }
                jsonPath("$.data.totalAmount") { value(39_800) }
                jsonPath("$.data.items[0].productName") { value("주문상품2") }
                jsonPath("$.data.items[0].quantity") { value(2) }
            }

        val updatedOrder = orderJpaRepository.findById(order.id).orElseThrow()
        updatedOrder.totalAmount.amount shouldBe 39_800
        updatedOrder.items.single().productId shouldBe anotherProduct.id
        updatedOrder.items.single().status shouldBe OrderItemStatus.CREATED
    }

    @Test
    fun `결제 완료 주문은 수정할 수 없다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-PAID",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                    status = OrderStatus.PAID,
                ),
            )

        mockMvc
            .patch("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                      "items": [
                        {
                          "productId": ${product.id},
                          "productOptionId": ${product.options.single().id},
                          "quantity": 1
                        }
                      ]
                    }
                    """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.message") { value("수정할 수 없는 주문입니다.") }
            }
    }

    @Test
    fun `타인 주문 상세는 조회할 수 없다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val otherMember =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 2,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = otherMember.id,
                    product = product,
                    orderNumber = "ORD-PRIVATE",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .get("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.error.code") { value("NOT_FOUND") }
            }
    }

    @Test
    fun `인증된 회원은 결제 전 주문을 삭제할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-DELETE",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                ),
            )

        mockMvc
            .delete("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val deletedOrder = orderJpaRepository.findById(order.id).orElseThrow()
        deletedOrder.status shouldBe OrderStatus.CANCELED
        deletedOrder.deletedAt.shouldNotBeNull()

        mockMvc
            .get("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.error.code") { value("NOT_FOUND") }
            }
    }

    @Test
    fun `결제 완료 주문은 삭제할 수 없다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.create(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-PAID-DELETE",
                    orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                    status = OrderStatus.PAID,
                ),
            )

        mockMvc
            .delete("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.message") { value("삭제할 수 없는 주문입니다.") }
            }
    }

    @Test
    fun `인증된 회원은 배송 완료 주문상품을 구매 확정할 수 있다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.createDelivered(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-DELIVERED-${member.id}-2026-03-22",
                    deliveredAt = LocalDateTime.of(2026, 3, 22, 9, 0),
                ),
            )
        val orderItemId = order.items.single().id

        mockMvc
            .post("/api/orders/items/$orderItemId/purchase-confirmation") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val confirmedOrder = orderJpaRepository.findById(order.id).orElseThrow()
        val confirmedItem = confirmedOrder.items.single()
        confirmedItem.status shouldBe OrderItemStatus.PURCHASE_CONFIRMED
        confirmedItem.confirmedAt.shouldNotBeNull()
        confirmedItem.optionColorSnapshot shouldBe "BLACK"
        confirmedItem.optionSizeSnapshot shouldBe "M"
    }

    @Test
    fun `이미 구매 확정된 주문상품은 다시 구매 확정할 수 없다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.createIndexedJpa(
                    index = 1,
                    emailPrefix = "order-member",
                    nicknamePrefix = "order-nickname",
                    phoneNumberPrefix = "0101000000",
                ),
            )
        val product =
            productJpaRepository.save(
                ProductFixture.createSingleOption(
                    index = 1,
                    namePrefix = "주문상품",
                    descriptionPrefix = "주문상품 설명",
                    imagePrefix = "order-product",
                ),
            )
        val order =
            orderJpaRepository.save(
                OrderFixture.createDelivered(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-DELIVERED-${member.id}-2026-03-22",
                    deliveredAt = LocalDateTime.of(2026, 3, 22, 9, 0),
                ),
            )
        val orderItemId = order.items.single().id

        mockMvc
            .post("/api/orders/items/$orderItemId/purchase-confirmation") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isOk() }
            }

        mockMvc
            .post("/api/orders/items/$orderItemId/purchase-confirmation") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = member.id))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }
    }
}

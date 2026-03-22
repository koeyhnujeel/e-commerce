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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.ProductJpaRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductImage
import zoonza.commerce.catalog.domain.ProductOption
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.member.domain.Member
import zoonza.commerce.order.adapter.out.persistence.OrderJpaRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.shared.Email
import zoonza.commerce.shared.Money
import zoonza.commerce.support.MySqlTestContainerConfig
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
        val member = insertMember(index = 1)
        val product = insertProduct(index = 1)
        val productOption = product.options.single()

        mockMvc
            .post("/api/orders") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
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
        val member = insertMember(index = 1)
        val otherMember = insertMember(index = 2)
        val product = insertProduct(index = 1)

        insertCreatedOrder(
            memberId = member.id,
            product = product,
            orderNumber = "ORD-OLD",
            orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
        )
        insertCreatedOrder(
            memberId = member.id,
            product = product,
            orderNumber = "ORD-NEW",
            orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
        )
        insertCreatedOrder(
            memberId = otherMember.id,
            product = product,
            orderNumber = "ORD-OTHER",
            orderedAt = LocalDateTime.of(2026, 3, 23, 10, 0),
        )

        mockMvc
            .get("/api/orders") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
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
        val member = insertMember(index = 1)
        val product = insertProduct(index = 1)
        val order =
            insertCreatedOrder(
                memberId = member.id,
                product = product,
                orderNumber = "ORD-DETAIL",
                orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
            )

        mockMvc
            .get("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
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
    fun `타인 주문 상세는 조회할 수 없다`() {
        val member = insertMember(index = 1)
        val otherMember = insertMember(index = 2)
        val product = insertProduct(index = 1)
        val order =
            insertCreatedOrder(
                memberId = otherMember.id,
                product = product,
                orderNumber = "ORD-PRIVATE",
                orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
            )

        mockMvc
            .get("/api/orders/${order.id}") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.error.code") { value("NOT_FOUND") }
            }
    }

    @Test
    fun `인증된 회원은 배송 완료 주문상품을 구매 확정할 수 있다`() {
        val member = insertMember(index = 1)
        val product = insertProduct(index = 1)
        val order = insertDeliveredOrder(member.id, product, LocalDateTime.of(2026, 3, 22, 9, 0))
        val orderItemId = order.items.single().id

        mockMvc
            .post("/api/orders/items/$orderItemId/purchase-confirmation") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
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
        val member = insertMember(index = 1)
        val product = insertProduct(index = 1)
        val order = insertDeliveredOrder(member.id, product, LocalDateTime.of(2026, 3, 22, 9, 0))
        val orderItemId = order.items.single().id

        mockMvc
            .post("/api/orders/items/$orderItemId/purchase-confirmation") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
            }.andExpect {
                status { isOk() }
            }

        mockMvc
            .post("/api/orders/items/$orderItemId/purchase-confirmation") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(member))
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }
    }

    private fun insertMember(index: Int): Member {
        return memberJapRepository.save(
            Member.create(
                email = Email("order-member$index@example.com"),
                passwordHash = "encoded-password",
                name = "회원$index",
                nickname = "order-nickname$index",
                phoneNumber = "0101000000$index",
                registeredAt = LocalDateTime.of(2026, 3, 21, 8, 0),
            ),
        )
    }

    private fun insertProduct(index: Int): Product {
        return productJpaRepository.save(
            Product.create(
                brandId = 1L,
                name = "주문상품$index",
                description = "주문상품 설명$index",
                basePrice = Money(19_900),
                categoryIds = listOf(1L),
                images =
                    listOf(
                        ProductImage.create(
                            imageUrl = "https://cdn.example.com/order-product-$index-primary.jpg",
                            isPrimary = true,
                            sortOrder = 0,
                        ),
                    ),
                options =
                    listOf(
                        ProductOption.create(
                            color = "BLACK",
                            size = "M",
                            stockId = index.toLong(),
                        ),
                    ),
            ),
        )
    }

    private fun insertDeliveredOrder(
        memberId: Long,
        product: Product,
        deliveredAt: LocalDateTime,
    ): Order {
        val productOption = product.options.single()

        return orderJpaRepository.save(
            Order.create(
                memberId = memberId,
                orderNumber = "ORD-DELIVERED-$memberId-${deliveredAt.toLocalDate()}",
                status = OrderStatus.DELIVERED,
                orderedAt = deliveredAt.minusDays(2),
                deliveredAt = deliveredAt,
                items =
                    listOf(
                        OrderItem.create(
                            productId = product.id,
                            productOptionId = productOption.id,
                            productNameSnapshot = product.name,
                            optionColorSnapshot = productOption.color,
                            optionSizeSnapshot = productOption.size,
                            quantity = 1,
                            orderPrice = product.basePrice,
                        ),
                    ),
            ),
        )
    }

    private fun insertCreatedOrder(
        memberId: Long,
        product: Product,
        orderNumber: String,
        orderedAt: LocalDateTime,
    ): Order {
        val productOptionId = product.options.single().id
        val productOption = product.options.single()

        return orderJpaRepository.save(
            Order.create(
                memberId = memberId,
                orderNumber = orderNumber,
                status = OrderStatus.CREATED,
                orderedAt = orderedAt,
                items =
                    listOf(
                        OrderItem.create(
                            productId = product.id,
                            productOptionId = productOptionId,
                            productNameSnapshot = product.name,
                            optionColorSnapshot = productOption.color,
                            optionSizeSnapshot = productOption.size,
                            quantity = 1,
                            orderPrice = Money(19_900),
                        ),
                    ),
            ),
        )
    }

    private fun authorizationHeader(member: Member): String {
        val accessToken = accessTokenProvider.issue(member.id, member.email.address, member.role.name)
        return "Bearer $accessToken"
    }
}

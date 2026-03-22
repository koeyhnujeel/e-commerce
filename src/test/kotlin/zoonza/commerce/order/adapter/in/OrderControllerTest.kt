package zoonza.commerce.order.adapter.`in`

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
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
        val productOptionId = product.options.single().id

        return orderJpaRepository.save(
            Order.create(
                memberId = memberId,
                status = OrderStatus.DELIVERED,
                orderedAt = deliveredAt.minusDays(2),
                deliveredAt = deliveredAt,
                items =
                    listOf(
                        OrderItem.create(
                            productId = product.id,
                            productOptionId = productOptionId,
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

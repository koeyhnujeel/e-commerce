package zoonza.commerce.cart.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.cart.application.dto.CartItemUnavailableReason
import zoonza.commerce.cart.domain.Cart
import zoonza.commerce.cart.domain.CartRepository
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.ProductOptionSummary
import zoonza.commerce.inventory.InventoryApi

class DefaultCartServiceTest {
    private val cartRepository = mockk<CartRepository>()
    private val catalogApi = mockk<CatalogApi>()
    private val inventoryApi = mockk<InventoryApi>()
    private val cartService = DefaultCartService(cartRepository, catalogApi, inventoryApi)

    @Test
    fun `신규 장바구니면 생성 후 항목을 저장한다`() {
        val savedCart = slot<Cart>()
        every { catalogApi.validateAvailableProductOption(100L, 10L) } returns Unit
        every { cartRepository.findByMemberId(1L) } returns null
        every { cartRepository.save(capture(savedCart)) } answers { savedCart.captured }

        cartService.addItem(memberId = 1L, productId = 100L, productOptionId = 10L, quantity = 2L)

        savedCart.captured.memberId shouldBe 1L
        savedCart.captured.items.single().productId shouldBe 100L
        savedCart.captured.items.single().productOptionId shouldBe 10L
        savedCart.captured.items.single().quantity shouldBe 2L
        verify(exactly = 1) { catalogApi.validateAvailableProductOption(100L, 10L) }
        verify(exactly = 1) { cartRepository.save(any()) }
    }

    @Test
    fun `장바구니 조회는 재고 부족 항목을 구매 불가로 표시한다`() {
        val cart = Cart.create(memberId = 1L).apply {
            addItem(productId = 100L, productOptionId = 10L, quantity = 3L)
        }
        every { cartRepository.findByMemberId(1L) } returns cart
        every { catalogApi.getProductOptionSummaries(setOf(10L)) } returns
            mapOf(
                10L to ProductOptionSummary(
                    productId = 100L,
                    productOptionId = 10L,
                    productName = "상품",
                    primaryImageUrl = "https://cdn.example.com/product.jpg",
                    basePrice = 10_000L,
                    additionalPrice = 1_000L,
                    color = "BLACK",
                    size = "M",
                    availableForSale = true,
                ),
            )
        every { inventoryApi.getAvailableQuantities(setOf(10L)) } returns mapOf(10L to 1L)

        val result = cartService.getMyCart(memberId = 1L)

        result.items.single().purchasable shouldBe false
        result.items.single().unavailableReason shouldBe CartItemUnavailableReason.INSUFFICIENT_STOCK
        result.items.single().lineAmount shouldBe 33_000L
        result.summary.totalAmount shouldBe 33_000L
        result.summary.purchasableItemCount shouldBe 0
        result.summary.purchasableAmount shouldBe 0L
    }

    @Test
    fun `수량 변경 시 장바구니 항목의 상품 ID로 옵션 유효성을 다시 검증한다`() {
        val cart = Cart.create(memberId = 1L).apply {
            addItem(productId = 100L, productOptionId = 10L, quantity = 1L)
        }
        every { cartRepository.findByMemberId(1L) } returns cart
        every { catalogApi.validateAvailableProductOption(100L, 10L) } returns Unit
        every { cartRepository.save(any()) } answers { firstArg() }

        cartService.changeItemQuantity(memberId = 1L, productOptionId = 10L, quantity = 2L)

        cart.items.single().quantity shouldBe 2L
        verify(exactly = 1) { catalogApi.validateAvailableProductOption(100L, 10L) }
        verify(exactly = 1) { cartRepository.save(cart) }
    }

    @Test
    fun `장바구니가 없으면 빈 응답을 반환한다`() {
        every { cartRepository.findByMemberId(1L) } returns null

        val result = cartService.getMyCart(memberId = 1L)

        result.items shouldBe emptyList()
        result.summary.itemCount shouldBe 0
        verify(exactly = 0) { catalogApi.getProductOptionSummaries(any()) }
        verify(exactly = 0) { inventoryApi.getAvailableQuantities(any()) }
    }
}

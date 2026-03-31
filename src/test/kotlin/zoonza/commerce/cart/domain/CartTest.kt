package zoonza.commerce.cart.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CartTest {
    @Test
    fun `같은 상품 옵션을 다시 담으면 수량을 합산한다`() {
        val cart = Cart.create(memberId = 1L)

        cart.addItem(productId = 1L, productOptionId = 10L, quantity = 2L)
        cart.addItem(productId = 1L, productOptionId = 10L, quantity = 3L)

        cart.items shouldHaveSize 1
        cart.items.single().productId shouldBe 1L
        cart.items.single().productOptionId shouldBe 10L
        cart.items.single().quantity shouldBe 5L
    }

    @Test
    fun `장바구니 항목 수량을 변경할 수 있다`() {
        val cart = Cart.create(memberId = 1L)
        cart.addItem(productId = 1L, productOptionId = 10L, quantity = 2L)

        cart.changeQuantity(productOptionId = 10L, quantity = 7L)

        cart.items.single().quantity shouldBe 7L
    }

    @Test
    fun `같은 옵션에 다른 상품 ID를 넣으면 예외가 발생한다`() {
        val cart = Cart.create(memberId = 1L)
        cart.addItem(productId = 1L, productOptionId = 10L, quantity = 2L)

        val exception = shouldThrow<IllegalArgumentException> {
            cart.addItem(productId = 2L, productOptionId = 10L, quantity = 1L)
        }

        exception.message shouldBe "상품 ID와 상품 옵션 ID 조합이 올바르지 않습니다."
    }

    @Test
    fun `없는 장바구니 항목을 삭제하면 예외가 발생한다`() {
        val cart = Cart.create(memberId = 1L)

        val exception = shouldThrow<zoonza.commerce.shared.BusinessException> {
            cart.removeItem(productOptionId = 99L)
        }

        exception.errorCode shouldBe CartErrorCode.CART_ITEM_NOT_FOUND
    }

    @Test
    fun `전체 비우기를 하면 모든 항목이 제거된다`() {
        val cart = Cart.create(memberId = 1L)
        cart.addItem(productId = 1L, productOptionId = 10L, quantity = 2L)
        cart.addItem(productId = 2L, productOptionId = 20L, quantity = 1L)

        cart.clear()

        cart.items shouldHaveSize 0
    }
}

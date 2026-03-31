package zoonza.commerce.cart.adapter.out.persistence

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.cart.domain.Cart
import zoonza.commerce.cart.domain.CartErrorCode
import zoonza.commerce.cart.domain.CartRepository
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.support.MySqlTestContainerConfig

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class CartRepositoryAdapterTest {
    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartJpaRepository: CartJpaRepository

    @Test
    fun `장바구니 aggregate를 저장하고 조회한다`() {
        val cart = Cart.create(memberId = 1L).apply {
            addItem(productId = 100L, productOptionId = 10L, quantity = 2L)
            addItem(productId = 200L, productOptionId = 20L, quantity = 1L)
        }

        val saved = cartRepository.save(cart)
        val found = cartRepository.findByMemberId(1L)
        val actual = found.shouldNotBeNull()

        saved.id shouldBe actual.id
        actual.items shouldHaveSize 2
        actual.items.first().productId shouldBe 100L
        actual.items.first().productOptionId shouldBe 10L
    }

    @Test
    fun `장바구니 수정 저장 시 기존 항목을 갱신하고 제거한다`() {
        val saved =
            cartRepository.save(
                Cart.create(memberId = 1L).apply {
                    addItem(productId = 100L, productOptionId = 10L, quantity = 2L)
                    addItem(productId = 200L, productOptionId = 20L, quantity = 1L)
                },
            )

        saved.changeQuantity(productOptionId = 10L, quantity = 5L)
        saved.removeItem(productOptionId = 20L)
        cartRepository.save(saved)

        val found = cartRepository.findByMemberId(1L)
        val actual = found.shouldNotBeNull()

        actual.items shouldHaveSize 1
        actual.items.single().productOptionId shouldBe 10L
        actual.items.single().quantity shouldBe 5L
    }

    @Test
    fun `회원당 장바구니는 하나만 저장할 수 있다`() {
        cartJpaRepository.saveAndFlush(CartJpaEntity.from(Cart.create(memberId = 1L)))

        assertThrows<DataIntegrityViolationException> {
            cartJpaRepository.saveAndFlush(CartJpaEntity.from(Cart.create(memberId = 1L)))
        }
    }

    @Test
    fun `이전 버전의 장바구니를 저장하면 동시 수정 예외가 발생한다`() {
        cartRepository.save(
            Cart.create(memberId = 1L).apply {
                addItem(productId = 100L, productOptionId = 10L, quantity = 1L)
            },
        )

        val firstRead = cartRepository.findByMemberId(1L).shouldNotBeNull()
        val secondRead = cartRepository.findByMemberId(1L).shouldNotBeNull()

        firstRead.changeQuantity(productOptionId = 10L, quantity = 2L)
        cartRepository.save(firstRead)

        secondRead.changeQuantity(productOptionId = 10L, quantity = 3L)

        val exception = assertThrows<BusinessException> {
            cartRepository.save(secondRead)
        }

        exception.errorCode shouldBe CartErrorCode.CONCURRENT_CART_MODIFICATION
    }
}

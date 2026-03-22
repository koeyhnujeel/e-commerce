package zoonza.commerce.shared

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MoneyTest {
    @Test
    fun `0 이상의 금액을 생성한다`() {
        val money = Money(10_000)

        money.amount shouldBe 10_000
    }

    @Test
    fun `음수 금액이면 예외를 던진다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Money(-1)
            }

        exception.message shouldBe "금액은 0 이상이어야 합니다."
    }

    @Test
    fun `금액을 더할 수 있다`() {
        val result = Money(10_000) + Money(2_500)

        result.amount shouldBe 12_500
    }

    @Test
    fun `금액에 수량을 곱할 수 있다`() {
        val result = Money(19_900).multiply(3)

        result.amount shouldBe 59_700
    }
}

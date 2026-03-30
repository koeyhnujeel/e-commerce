package zoonza.commerce.shared

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyTest {
    @Test
    fun `0 이상의 금액을 생성한다`() {
        val money = Money(10_000)

        money.amount shouldBe BigDecimal.valueOf(10_000)
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

        result.amount shouldBe BigDecimal.valueOf(12_500)
    }

    @Test
    fun `금액을 뺄 수 있다`() {
        val result = Money(10_000) - Money(2_500)

        result.amount shouldBe BigDecimal.valueOf(7_500)
    }

    @Test
    fun `보유 금액보다 큰 금액을 빼면 예외를 던진다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Money(2_500) - Money(10_000)
            }

        exception.message shouldBe "금액은 0 이상이어야 합니다."
    }

    @Test
    fun `금액에 수량을 곱할 수 있다`() {
        val result = Money(19_900) * 3

        result.amount shouldBe BigDecimal.valueOf(59_700)
    }

    @Test
    fun `소수점 이하 금액이면 예외를 던진다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Money(BigDecimal("10.5"))
            }

        exception.message shouldBe "금액은 소수점 이하를 허용하지 않습니다."
    }
}

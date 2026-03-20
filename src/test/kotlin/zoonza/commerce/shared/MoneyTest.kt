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
}

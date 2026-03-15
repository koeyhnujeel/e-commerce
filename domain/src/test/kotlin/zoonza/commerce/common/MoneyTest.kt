package zoonza.commerce.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MoneyTest {
    @Test
    fun `금액이 0 이상이면 생성한다`() {
        val money = Money(39_000L)

        money.amount shouldBe 39_000L
    }

    @Test
    fun `금액이 음수면 예외를 던진다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Money(-1L)
            }

        exception.message shouldBe "금액은 0 이상이어야 합니다."
    }
}

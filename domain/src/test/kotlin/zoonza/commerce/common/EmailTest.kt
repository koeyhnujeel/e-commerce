package zoonza.commerce.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmailTest {
    @Test
    fun `올바른 이메일 형식이면 생성한다`() {
        val email = Email("member@example.com")

        email.address shouldBe "member@example.com"
    }

    @Test
    fun `이메일 형식이 아니면 예외를 던진다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Email("invalid-email.com")
            }

        exception.message shouldBe "이메일 형식이 올바르지 않습니다."
    }
}

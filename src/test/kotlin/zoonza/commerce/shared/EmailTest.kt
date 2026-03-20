package zoonza.commerce.shared

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EmailTest {
    @Test
    fun `유효한 이메일을 생성한다`() {
        val email = Email("member@example.com")

        email.address shouldBe "member@example.com"
    }

    @Test
    fun `이메일 형식이 올바르지 않으면 예외를 던진다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Email("invalid-email")
            }

        exception.message shouldBe "이메일 형식이 올바르지 않습니다."
    }
}

package zoonza.commerce.notification.application.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.notification.application.port.out.EmailSender
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.VerificationCodeCreated

class VerificationEmailHandlerTest {
    private val emailSender = mockk<EmailSender>(relaxed = true)
    private val verificationEmailHandler = VerificationEmailHandler(emailSender)

    @Test
    fun `인증 코드 생성 이벤트를 메일 발송으로 변환한다`() {
        verificationEmailHandler.handle(
            VerificationCodeCreated(
                email = Email("member@example.com"),
                code = "123 456",
            ),
        )

        verify(exactly = 1) {
            emailSender.send(
                to = "member@example.com",
                subject = "이메일 인증 코드",
                body = match { it.contains("123 456") },
            )
        }
    }
}

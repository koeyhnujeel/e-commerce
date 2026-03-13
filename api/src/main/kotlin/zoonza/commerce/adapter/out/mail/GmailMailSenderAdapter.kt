package zoonza.commerce.adapter.out.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import zoonza.commerce.common.Email
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.out.VerificationCodeSender

@Component
class GmailMailSenderAdapter(
    private val javaMailSender: JavaMailSender,
    @Value("\${spring.mail.username}")
    private val fromAddress: String,
) : VerificationCodeSender {
    override fun sendVerificationCode(
        to: Email,
        purpose: VerificationPurpose,
        code: String,
    ) {
        val message =
            SimpleMailMessage().apply {
                from = fromAddress
                setTo(to.address)
                subject = createSubject(purpose)
                text =
                    """
                    인증 코드는 $code 입니다.

                    본인이 요청하지 않았는데 이 메일을 받으셨다면 요청 여부를 확인해 보시기 바랍니다.
                    계정 보안이 우려되는 경우 비밀번호 변경을 권장드립니다.
                    """.trimIndent()
            }

        javaMailSender.send(message)
    }

    private fun createSubject(purpose: VerificationPurpose): String {
        return when (purpose) {
            VerificationPurpose.SIGNUP -> "이메일 인증 코드"
            VerificationPurpose.PASSWORD_RESET -> "비밀번호 재설정 인증 코드"
        }
    }
}

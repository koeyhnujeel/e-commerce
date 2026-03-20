package zoonza.commerce.notification.application.service

import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import zoonza.commerce.notification.application.port.out.EmailSender
import zoonza.commerce.verification.VerificationCodeCreated

@Service
class VerificationEmailHandler(
    private val emailSender: EmailSender,
) {
    @ApplicationModuleListener
    fun handle(event: VerificationCodeCreated) {
        emailSender.send(
            to = event.email.address,
            subject = "이메일 인증 코드",
            body = """
                    인증 코드는 ${event.code} 입니다.

                    본인이 요청하지 않았는데 이 메일을 받으셨다면 요청 여부를 확인해 보시기 바랍니다.
                    계정 보안이 우려되는 경우 비밀번호 변경을 권장드립니다.
                    """.trimIndent()
        )
    }
}
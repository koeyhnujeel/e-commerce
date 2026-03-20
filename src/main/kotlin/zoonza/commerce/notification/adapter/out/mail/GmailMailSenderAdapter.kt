package zoonza.commerce.notification.adapter.out.mail

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component
import zoonza.commerce.notification.application.port.out.EmailSender

@Component
class GmailMailSenderAdapter(
    private val javaMailSender: JavaMailSender,
) : EmailSender {
    override fun send(to: String, subject: String, body: String) {
        val message = SimpleMailMessage().apply {
            setTo(to)
            setSubject(subject)
            text = body
        }

        javaMailSender.send(message)
    }
}
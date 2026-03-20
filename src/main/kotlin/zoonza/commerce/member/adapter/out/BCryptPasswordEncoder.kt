package zoonza.commerce.member.adapter.out

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import zoonza.commerce.member.domain.PasswordEncoder

@Component
class BCryptPasswordEncoder : PasswordEncoder {
    private val passwordEncoder = BCryptPasswordEncoder()

    override fun encode(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }

    override fun matches(
        rawPassword: String,
        passwordHash: String,
    ): Boolean {
        return passwordEncoder.matches(rawPassword, passwordHash)
    }
}

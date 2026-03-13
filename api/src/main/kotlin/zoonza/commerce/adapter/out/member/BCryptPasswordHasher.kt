package zoonza.commerce.adapter.out.member

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import zoonza.commerce.member.port.out.PasswordHasher

@Component
class BCryptPasswordHasher : PasswordHasher {
    private val passwordEncoder = BCryptPasswordEncoder()

    override fun hash(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }
}

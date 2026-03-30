package zoonza.commerce.shared

import kotlin.jvm.JvmInline

@JvmInline
value class Email(val address: String) {
    init {
        require(EMAIL_PATTERN.matches(address)) {
            "이메일 형식이 올바르지 않습니다."
        }
    }

    companion object {
        private val EMAIL_PATTERN =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

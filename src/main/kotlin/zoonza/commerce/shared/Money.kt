package zoonza.commerce.shared

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Money(
    @Column(nullable = false)
    val amount: Long,
) {
    init {
        require(amount >= 0) {
            "금액은 0 이상이어야 합니다."
        }
    }

    operator fun plus(other: Money): Money {
        return Money(amount + other.amount)
    }

    fun multiply(multiplier: Int): Money {
        require(multiplier >= 0) { "배수는 0 이상이어야 합니다." }
        return Money(amount * multiplier)
    }
}

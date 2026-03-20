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
}

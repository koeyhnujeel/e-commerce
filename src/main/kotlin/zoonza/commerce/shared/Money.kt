package zoonza.commerce.shared

import java.math.BigDecimal

@JvmInline
value class Money(val amount: BigDecimal) {
    init {
        require(amount >= BigDecimal.ZERO) {
            "금액은 0 이상이어야 합니다."
        }
        require(amount.scale() == 0) {
            "금액은 소수점 이하를 허용하지 않습니다."
        }
    }

    constructor(amount: Long) : this(BigDecimal.valueOf(amount))

    constructor(amount: Int) : this(amount.toLong())

    operator fun plus(other: Money): Money {
        return Money(amount + other.amount)
    }

    operator fun minus(other: Money): Money {
        return Money(amount - other.amount)
    }

    operator fun times(multiplier: Int): Money {
        require(multiplier >= 0) { "배수는 0 이상이어야 합니다." }
        return Money(amount.multiply(BigDecimal.valueOf(multiplier.toLong())))
    }
}

package zoonza.commerce.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_token",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_refresh_token_member_id",
            columnNames = ["member_id"],
        ),
    ],
)
class RefreshToken private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    var token: String,

    @Column(nullable = false)
    var issuedAt: LocalDateTime,

    @Column(nullable = false)
    var expiresAt: LocalDateTime,
) {
    fun rotate(
        token: String,
        issuedAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ) {
        this.token = token
        this.issuedAt = issuedAt
        this.expiresAt = expiresAt
    }

    companion object {
        fun issue(
            memberId: Long,
            token: String,
            issuedAt: LocalDateTime,
            expiresAt: LocalDateTime,
        ): RefreshToken {
            return RefreshToken(
                memberId = memberId,
                token = token,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )
        }
    }
}

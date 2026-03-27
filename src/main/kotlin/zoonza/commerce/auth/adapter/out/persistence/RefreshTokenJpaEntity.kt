package zoonza.commerce.auth.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.auth.domain.RefreshToken
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
class RefreshTokenJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val token: String = "",

    @Column(nullable = false)
    val issuedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.MIN,
) {
    companion object {
        fun from(refreshToken: RefreshToken): RefreshTokenJpaEntity {
            return RefreshTokenJpaEntity(
                id = refreshToken.id,
                memberId = refreshToken.memberId,
                token = refreshToken.token,
                issuedAt = refreshToken.issuedAt,
                expiresAt = refreshToken.expiresAt,
            )
        }
    }

    fun toDomain(): RefreshToken {
        return RefreshToken(
            id = id,
            memberId = memberId,
            token = token,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
        )
    }
}

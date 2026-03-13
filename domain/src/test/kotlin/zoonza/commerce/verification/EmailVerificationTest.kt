package zoonza.commerce.verification

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import java.time.LocalDateTime
import kotlin.test.Test

class EmailVerificationTest {
    @Test
    fun `올바른 인증 코드면 인증 완료 시각을 기록한다`() {
        val verification =
            EmailVerification.issue(
                email = Email("member@example.com"),
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 13, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 13, 10, 5),
            )
        val verifiedAt = LocalDateTime.of(2026, 3, 13, 10, 3)

        verification.verify(code = "123 456", verifiedAt = verifiedAt)

        verification.verifiedAt shouldBe verifiedAt
    }

    @Test
    fun `인증 코드가 다르면 예외를 던진다`() {
        val verification =
            EmailVerification.issue(
                email = Email("member@example.com"),
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 13, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 13, 10, 5),
            )

        val exception =
            shouldThrow<BusinessException> {
                verification.verify(
                    code = "654 321",
                    verifiedAt = LocalDateTime.of(2026, 3, 13, 10, 3),
                )
            }

        exception.errorCode shouldBe ErrorCode.INVALID_VERIFICATION_CODE
    }

    @Test
    fun `만료된 인증 코드면 예외를 던진다`() {
        val verification =
            EmailVerification.issue(
                email = Email("member@example.com"),
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 13, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 13, 10, 5),
            )

        val exception =
            shouldThrow<BusinessException> {
                verification.verify(
                    code = "123 456",
                    verifiedAt = LocalDateTime.of(2026, 3, 13, 10, 6),
                )
            }

        exception.errorCode shouldBe ErrorCode.EXPIRED_VERIFICATION_CODE
    }

    @Test
    fun `인증이 완료되지 않았으면 예외를 던진다`() {
        val verification =
            EmailVerification.issue(
                email = Email("member@example.com"),
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 13, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 13, 10, 5),
            )

        val exception =
            shouldThrow<BusinessException> {
                verification.assertVerified()
            }

        exception.errorCode shouldBe ErrorCode.EMAIL_NOT_VERIFIED
    }

    @Test
    fun `인증이 완료됐으면 검증을 통과한다`() {
        val verification =
            EmailVerification.issue(
                email = Email("member@example.com"),
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 13, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 13, 10, 5),
            )

        verification.verify(
            code = "123 456",
            verifiedAt = LocalDateTime.of(2026, 3, 13, 10, 3),
        )

        verification.assertVerified()
    }
}

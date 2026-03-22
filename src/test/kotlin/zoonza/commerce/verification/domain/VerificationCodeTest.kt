package zoonza.commerce.verification.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.VerificationErrorCode
import java.time.LocalDateTime

class VerificationCodeTest {
    @Test
    fun `올바른 코드면 인증 시각을 기록한다`() {
        val verificationCode = createVerificationCode()
        val verifiedAt = LocalDateTime.of(2026, 3, 21, 10, 1)

        verificationCode.verify("123 456", verifiedAt)

        verificationCode.verifiedAt shouldBe verifiedAt
    }

    @Test
    fun `인증 코드가 다르면 예외를 던진다`() {
        val verificationCode = createVerificationCode()

        val exception =
            shouldThrow<BusinessException> {
                verificationCode.verify("654 321", LocalDateTime.of(2026, 3, 21, 10, 1))
            }

        exception.errorCode shouldBe VerificationErrorCode.INVALID_VERIFICATION_CODE
        verificationCode.verifiedAt.shouldBeNull()
    }

    @Test
    fun `만료된 인증 코드는 검증할 수 없다`() {
        val verificationCode =
            VerificationCode.create(
                email = Email("member@example.com"),
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 21, 10, 5),
            )

        val exception =
            shouldThrow<BusinessException> {
                verificationCode.verify("123 456", LocalDateTime.of(2026, 3, 21, 10, 6))
            }

        exception.errorCode shouldBe VerificationErrorCode.EXPIRED_VERIFICATION_CODE
    }

    @Test
    fun `갱신하면 코드와 만료 시간이 바뀌고 인증 상태를 초기화한다`() {
        val verificationCode = createVerificationCode()
        verificationCode.verify("123 456", LocalDateTime.of(2026, 3, 21, 10, 1))

        val renewedIssuedAt = LocalDateTime.of(2026, 3, 21, 11, 0)
        val renewedExpiresAt = renewedIssuedAt.plusMinutes(5)

        verificationCode.renew(
            code = "654 321",
            issuedAt = renewedIssuedAt,
            expiresAt = renewedExpiresAt,
        )

        verificationCode.code shouldBe "654 321"
        verificationCode.issuedAt shouldBe renewedIssuedAt
        verificationCode.expiresAt shouldBe renewedExpiresAt
        verificationCode.verifiedAt.shouldBeNull()
    }

    @Test
    fun `인증되지 않은 상태에서 검증 완료를 주장하면 예외를 던진다`() {
        val verificationCode = createVerificationCode()

        val exception =
            shouldThrow<BusinessException> {
                verificationCode.assertVerified()
            }

        exception.errorCode shouldBe VerificationErrorCode.EMAIL_NOT_VERIFIED
    }

    private fun createVerificationCode(): VerificationCode {
        return VerificationCode.create(
            email = Email("member@example.com"),
            purpose = VerificationPurpose.SIGNUP,
            code = "123 456",
            issuedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            expiresAt = LocalDateTime.of(2026, 3, 21, 10, 5),
        )
    }
}

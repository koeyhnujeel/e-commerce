package zoonza.commerce.member.adapter.`in`

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.member.adapter.`in`.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.member.adapter.`in`.request.SignupMemberRequest
import zoonza.commerce.member.adapter.`in`.request.VerifySignupEmailVerificationCodeRequest
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.member.application.port.out.NicknameGenerator
import zoonza.commerce.member.domain.PasswordEncoder
import zoonza.commerce.notification.application.port.out.EmailSender
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.MemberFixture
import zoonza.commerce.support.fixture.VerificationCodeFixture
import zoonza.commerce.verification.adapter.out.persistence.VerificationCodeJpaEntity
import zoonza.commerce.verification.adapter.out.persistence.VerificationCodeJpaRepository
import zoonza.commerce.verification.domain.VerificationPurpose
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class, MemberControllerTest.TestConfig::class)
class MemberControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberJapRepository: MemberJapRepository

    @Autowired
    private lateinit var verificationCodeJpaRepository: VerificationCodeJpaRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `회원가입 이메일 인증 코드 발송 요청에 성공한다`() {
        val request = SendSignupEmailVerificationCodeRequest(email = "member@example.com")

        mockMvc
            .post("/api/members/signup/email-verifications") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.error") { doesNotExist() }
            }

        val verification =
            verificationCodeJpaRepository.findByEmailAndPurpose(
                request.email,
                VerificationPurpose.SIGNUP,
            )

        verification.shouldNotBeNull()
        verification.code.length shouldBe 7
    }

    @Test
    fun `이미 사용 중인 이메일이면 충돌 응답을 반환한다`() {
        memberJapRepository.save(
            MemberFixture.createJpa(
                email = "member@example.com",
                passwordHash = passwordEncoder.encode("Password123!"),
                name = "기존회원",
                nickname = "existing-nickname-01012345678",
                phoneNumber = "01012345678",
                registeredAt = LocalDateTime.now(),
            ),
        )

        mockMvc
            .post("/api/members/signup/email-verifications") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    SendSignupEmailVerificationCodeRequest(email = "member@example.com"),
                )
            }.andExpect {
                status { isConflict() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("CONFLICT") }
            }
    }

    @Test
    fun `회원가입 이메일 인증 코드 검증에 성공한다`() {
        verificationCodeJpaRepository.save(
            VerificationCodeFixture.createJpa(
                email = "member@example.com",
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            ),
        )

        mockMvc
            .post("/api/members/signup/email-verifications/verify") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    VerifySignupEmailVerificationCodeRequest(
                        email = "member@example.com",
                        code = "123 456",
                    ),
                )
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val verification =
            verificationCodeJpaRepository.findByEmailAndPurpose(
                "member@example.com",
                VerificationPurpose.SIGNUP,
            )

        verification.shouldNotBeNull()
        verification.verifiedAt.shouldNotBeNull()
    }

    @Test
    fun `검증된 이메일이면 회원가입에 성공한다`() {
        val verification =
            VerificationCodeFixture.create(
                email = "member@example.com",
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )
        verification.verify("123 456", LocalDateTime.now())
        verificationCodeJpaRepository.save(VerificationCodeJpaEntity.from(verification))

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    SignupMemberRequest(
                        email = "member@example.com",
                        password = "Password123!",
                        name = "주문자",
                        phoneNumber = "01012345678",
                    ),
                )
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { isNumber() }
            }

        val member = memberJapRepository.findByEmail("member@example.com")
        member.shouldNotBeNull()
        member.name shouldBe "주문자"
        member.phoneNumber shouldBe "01012345678"
    }

    @Test
    fun `회원가입 요청 본문이 잘못되면 잘못된 요청 응답을 반환한다`() {
        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    SignupMemberRequest(
                        email = "invalid-email",
                        password = "short",
                        name = "",
                        phoneNumber = "",
                    ),
                )
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun nicknameGenerator(): NicknameGenerator =
            object : NicknameGenerator {
                override fun generate(): String = "fixed-nickname"
            }

        @Bean
        @Primary
        fun emailSender(): EmailSender =
            object : EmailSender {
                override fun send(
                    to: String,
                    subject: String,
                    body: String,
                ) = Unit
            }
    }
}

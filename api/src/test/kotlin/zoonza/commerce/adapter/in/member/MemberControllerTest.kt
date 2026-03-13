package zoonza.commerce.adapter.`in`.member

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.adapter.`in`.member.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.adapter.out.persistence.member.MemberJapRepository
import zoonza.commerce.adapter.out.persistence.verification.EmailVerificationJpaRepository
import zoonza.commerce.common.Email
import zoonza.commerce.member.Member
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.out.VerificationCodeSender
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
    private lateinit var memberJpaRepository: MemberJapRepository

    @Autowired
    private lateinit var emailVerificationJpaRepository: EmailVerificationJpaRepository

    @Autowired
    private lateinit var verificationCodeSender: RecordingVerificationCodeSender

    @BeforeEach
    fun setUp() {
        verificationCodeSender.clear()
    }

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
                jsonPath("$.data") { doesNotExist() }
                jsonPath("$.error") { doesNotExist() }
            }

        val verification =
            emailVerificationJpaRepository.findByEmailAddressAndPurpose(
                request.email,
                VerificationPurpose.SIGNUP,
            )

        verification.shouldNotBeNull()
        verification.email shouldBe Email(request.email)
        verification.purpose shouldBe VerificationPurpose.SIGNUP
        verificationCodeSender.sentCodes.shouldHaveSize(1)
    }

    @Test
    fun `이미 사용 중인 이메일이면 충돌 응답을 반환한다`() {
        val request = SendSignupEmailVerificationCodeRequest(email = "member@example.com")

        insertMember(email = request.email)

        mockMvc
            .post("/api/members/signup/email-verifications") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("CONFLICT") }
                jsonPath("$.error.message") { value("이미 사용 중인 이메일입니다.") }
            }

        val verification =
            emailVerificationJpaRepository.findByEmailAddressAndPurpose(
                request.email,
                VerificationPurpose.SIGNUP,
            )

        verification.shouldBeNull()
        verificationCodeSender.sentCodes.shouldHaveSize(0)
    }

    @Test
    fun `이메일 형식이 올바르지 않으면 잘못된 요청 응답을 반환한다`() {
        val request = SendSignupEmailVerificationCodeRequest(email = "invalid-email")

        mockMvc
            .post("/api/members/signup/email-verifications") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }

        emailVerificationJpaRepository.count() shouldBe 0
        verificationCodeSender.sentCodes.shouldHaveSize(0)
    }

    private fun insertMember(email: String) {
        memberJpaRepository.save(
            Member.create(
                email = Email(email),
                passwordHash = "encoded-password",
                name = "tester",
                nickname = "tester-${System.nanoTime()}",
                phoneNumber = "0101234${(1000..9999).random()}",
                registeredAt = LocalDateTime.now(),
            ),
        )
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun verificationCodeSender(): RecordingVerificationCodeSender {
            return RecordingVerificationCodeSender()
        }
    }
}

class RecordingVerificationCodeSender : VerificationCodeSender {
    val sentCodes: MutableList<SentVerificationCode> = mutableListOf()

    override fun sendVerificationCode(
        to: Email,
        purpose: VerificationPurpose,
        code: String,
    ) {
        sentCodes += SentVerificationCode(to = to, purpose = purpose, code = code)
    }

    fun clear() {
        sentCodes.clear()
    }
}

data class SentVerificationCode(
    val to: Email,
    val purpose: VerificationPurpose,
    val code: String,
)

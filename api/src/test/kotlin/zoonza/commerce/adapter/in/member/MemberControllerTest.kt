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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.adapter.`in`.member.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.adapter.`in`.member.request.SignupMemberRequest
import zoonza.commerce.adapter.`in`.member.request.VerifySignupEmailVerificationCodeRequest
import zoonza.commerce.adapter.out.persistence.member.MemberJapRepository
import zoonza.commerce.adapter.out.persistence.verification.EmailVerificationJpaRepository
import zoonza.commerce.common.Email
import zoonza.commerce.member.Member
import zoonza.commerce.member.port.out.NicknameGenerator
import zoonza.commerce.verification.EmailVerification
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

    @Autowired
    private lateinit var nicknameGenerator: FixedNicknameGenerator

    @BeforeEach
    fun setUp() {
        verificationCodeSender.clear()
        nicknameGenerator.reset()
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

    @Test
    fun `회원가입 이메일 인증 코드 검증에 성공한다`() {
        val request = VerifySignupEmailVerificationCodeRequest(email = "member@example.com", code = "123 456")
        insertVerification(
            email = request.email,
            code = request.code,
            issuedAt = LocalDateTime.now().minusMinutes(1),
            expiresAt = LocalDateTime.now().plusMinutes(4),
        )

        mockMvc
            .post("/api/members/signup/email-verifications/verify") {
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
        verification.verifiedAt.shouldNotBeNull()
    }

    @Test
    fun `이메일 인증 요청이 없으면 찾을 수 없다는 응답을 반환한다`() {
        val request = VerifySignupEmailVerificationCodeRequest(email = "member@example.com", code = "123 456")

        mockMvc
            .post("/api/members/signup/email-verifications/verify") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("NOT_FOUND") }
                jsonPath("$.error.message") { value("이메일 인증 요청을 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `인증 코드가 다르면 잘못된 요청 응답을 반환한다`() {
        val request = VerifySignupEmailVerificationCodeRequest(email = "member@example.com", code = "654 321")
        insertVerification(
            email = request.email,
            code = "123 456",
            issuedAt = LocalDateTime.now().minusMinutes(1),
            expiresAt = LocalDateTime.now().plusMinutes(4),
        )

        mockMvc
            .post("/api/members/signup/email-verifications/verify") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.message") { value("인증 코드가 올바르지 않습니다.") }
            }

        val verification =
            emailVerificationJpaRepository.findByEmailAddressAndPurpose(
                request.email,
                VerificationPurpose.SIGNUP,
            )

        verification.shouldNotBeNull()
        verification.verifiedAt.shouldBeNull()
    }

    @Test
    fun `인증 코드가 만료되면 잘못된 요청 응답을 반환한다`() {
        val request = VerifySignupEmailVerificationCodeRequest(email = "member@example.com", code = "123 456")
        insertVerification(
            email = request.email,
            code = request.code,
            issuedAt = LocalDateTime.now().minusMinutes(10),
            expiresAt = LocalDateTime.now().minusMinutes(5),
        )

        mockMvc
            .post("/api/members/signup/email-verifications/verify") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.message") { value("인증 코드가 만료되었습니다.") }
            }

        val verification =
            emailVerificationJpaRepository.findByEmailAddressAndPurpose(
                request.email,
                VerificationPurpose.SIGNUP,
            )

        verification.shouldNotBeNull()
        verification.verifiedAt.shouldBeNull()
    }

    @Test
    fun `인증 코드 형식이 올바르지 않으면 잘못된 요청 응답을 반환한다`() {
        val request = VerifySignupEmailVerificationCodeRequest(email = "member@example.com", code = "123456")

        mockMvc
            .post("/api/members/signup/email-verifications/verify") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }

        emailVerificationJpaRepository.count() shouldBe 0
    }

    @Test
    fun `회원가입에 성공한다`() {
        val request =
            SignupMemberRequest(
                email = "member@example.com",
                password = "Password123!",
                name = "홍길동",
                phoneNumber = "01012345678",
            )
        insertVerification(
            email = request.email,
            code = "123 456",
            issuedAt = LocalDateTime.now().minusMinutes(1),
            expiresAt = LocalDateTime.now().plusMinutes(4),
            verifiedAt = LocalDateTime.now(),
        )

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { exists() }
                jsonPath("$.error") { doesNotExist() }
            }

        val member = memberJpaRepository.findByEmailAddress(request.email)

        member.shouldNotBeNull()
        member.email shouldBe Email(request.email)
        member.name shouldBe request.name
        member.phoneNumber shouldBe request.phoneNumber
        member.nickname shouldBe "반짝이는판다1"
        (member.passwordHash == request.password) shouldBe false
        BCryptPasswordEncoder().matches(request.password, member.passwordHash) shouldBe true
    }

    @Test
    fun `회원가입 시 이미 사용 중인 이메일이면 충돌 응답을 반환한다`() {
        val request =
            SignupMemberRequest(
                email = "member@example.com",
                password = "Password123!",
                name = "홍길동",
                phoneNumber = "01012345678",
            )
        insertMember(email = request.email)
        insertVerification(
            email = request.email,
            code = "123 456",
            issuedAt = LocalDateTime.now().minusMinutes(1),
            expiresAt = LocalDateTime.now().plusMinutes(4),
            verifiedAt = LocalDateTime.now(),
        )

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("CONFLICT") }
                jsonPath("$.error.message") { value("이미 사용 중인 이메일입니다.") }
            }
    }

    @Test
    fun `회원가입 시 이미 사용 중인 휴대폰 번호면 충돌 응답을 반환한다`() {
        val request =
            SignupMemberRequest(
                email = "member@example.com",
                password = "Password123!",
                name = "홍길동",
                phoneNumber = "01012345678",
            )
        insertMember(email = "other@example.com", phoneNumber = request.phoneNumber)
        insertVerification(
            email = request.email,
            code = "123 456",
            issuedAt = LocalDateTime.now().minusMinutes(1),
            expiresAt = LocalDateTime.now().plusMinutes(4),
            verifiedAt = LocalDateTime.now(),
        )

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("CONFLICT") }
                jsonPath("$.error.message") { value("이미 사용 중인 휴대폰 번호입니다.") }
            }
    }

    @Test
    fun `회원가입 시 이메일 인증이 완료되지 않았으면 잘못된 요청 응답을 반환한다`() {
        val request =
            SignupMemberRequest(
                email = "member@example.com",
                password = "Password123!",
                name = "홍길동",
                phoneNumber = "01012345678",
            )
        insertVerification(
            email = request.email,
            code = "123 456",
            issuedAt = LocalDateTime.now().minusMinutes(1),
            expiresAt = LocalDateTime.now().plusMinutes(4),
        )

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.message") { value("이메일 인증이 완료되지 않았습니다.") }
            }
    }

    @Test
    fun `회원가입 요청 값이 비어 있으면 잘못된 요청 응답을 반환한다`() {
        val request =
            SignupMemberRequest(
                email = "member@example.com",
                password = "",
                name = "",
                phoneNumber = "",
            )

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
            }
    }

    @Test
    fun `회원가입 시 비밀번호 형식이 올바르지 않으면 잘못된 요청 응답을 반환한다`() {
        val request =
            SignupMemberRequest(
                email = "member@example.com",
                password = "password123!",
                name = "홍길동",
                phoneNumber = "01012345678",
            )

        mockMvc
            .post("/api/members/signup") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("BAD_REQUEST") }
                jsonPath("$.error.errors[0].field") { value("password") }
                jsonPath("$.error.errors[0].reason") {
                    value("비밀번호는 영어 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.")
                }
            }

        memberJpaRepository.findByEmailAddress(request.email).shouldBeNull()
    }

    private fun insertMember(
        email: String,
        phoneNumber: String = "0101234${(1000..9999).random()}",
    ) {
        memberJpaRepository.save(
            Member.create(
                email = Email(email),
                passwordHash = "encoded-password",
                name = "tester",
                nickname = "tester-${System.nanoTime()}",
                phoneNumber = phoneNumber,
                registeredAt = LocalDateTime.now(),
            ),
        )
    }

    private fun insertVerification(
        email: String,
        code: String,
        issuedAt: LocalDateTime,
        expiresAt: LocalDateTime,
        verifiedAt: LocalDateTime? = null,
    ) {
        val verification =
            EmailVerification.issue(
                email = Email(email),
                purpose = VerificationPurpose.SIGNUP,
                code = code,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )

        if (verifiedAt != null) {
            verification.verify(code, verifiedAt)
        }

        emailVerificationJpaRepository.save(verification)
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun verificationCodeSender(): RecordingVerificationCodeSender {
            return RecordingVerificationCodeSender()
        }

        @Bean
        @Primary
        fun nicknameGenerator(): FixedNicknameGenerator {
            return FixedNicknameGenerator()
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

class FixedNicknameGenerator : NicknameGenerator {
    private var nicknames: ArrayDeque<String> = ArrayDeque(listOf("반짝이는판다1"))

    override fun generate(): String {
        return nicknames.removeFirstOrNull() ?: "반짝이는판다1"
    }

    fun reset() {
        nicknames = ArrayDeque(listOf("반짝이는판다1"))
    }
}

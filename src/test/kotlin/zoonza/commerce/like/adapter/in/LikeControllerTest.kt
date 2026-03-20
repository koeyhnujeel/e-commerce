package zoonza.commerce.like.adapter.`in`

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.like.adapter.out.persistence.MemberLikeJpaRepository
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class LikeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider

    @Autowired
    private lateinit var memberLikeJpaRepository: MemberLikeJpaRepository

    @Test
    fun `인증된 회원은 상품 좋아요를 등록할 수 있다`() {
        mockMvc
            .post("/api/products/10/likes") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val savedMemberLike = memberLikeJpaRepository.findByMemberIdAndTargetIdAndTargetType(1L, 10L, LikeTargetType.PRODUCT)
        savedMemberLike.shouldNotBeNull()
        savedMemberLike.deletedAt.shouldBeNull()
    }

    @Test
    fun `인증된 회원은 상품 좋아요를 취소할 수 있다`() {
        memberLikeJpaRepository.save(MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT))

        mockMvc
            .post("/api/products/10/likes/cancel") {
                header(HttpHeaders.AUTHORIZATION, authorizationHeader(memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val savedMemberLike = memberLikeJpaRepository.findByMemberIdAndTargetIdAndTargetType(1L, 10L, LikeTargetType.PRODUCT)
        savedMemberLike.shouldNotBeNull()
        savedMemberLike.deletedAt.shouldNotBeNull()
    }

    private fun authorizationHeader(memberId: Long): String {
        val accessToken = accessTokenProvider.issue(memberId, "member@example.com", "CUSTOMER")
        return "Bearer $accessToken"
    }
}

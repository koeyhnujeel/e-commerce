package zoonza.commerce.like.adapter.`in`

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.like.adapter.out.persistence.MemberLikeJpaEntity
import zoonza.commerce.like.adapter.out.persistence.MemberLikeJpaRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.ProductFixture

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
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var memberLikeJpaRepository: MemberLikeJpaRepository

    @Test
    fun `인증된 회원은 상품별 좋아요 여부를 조회할 수 있다`() {
        val likedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 10))
        val unlikedProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 20))
        memberLikeJpaRepository.save(
            MemberLikeJpaEntity.from(
                MemberLike.create(memberId = 1L, targetId = likedProduct.id, likeTargetType = LikeTargetType.PRODUCT),
            ),
        )

        mockMvc
            .get("/api/products/likes") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
                param("productIds", likedProduct.id.toString(), unlikedProduct.id.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].productId") { value(likedProduct.id) }
                jsonPath("$.data[0].liked") { value(true) }
                jsonPath("$.data[1].productId") { value(unlikedProduct.id) }
                jsonPath("$.data[1].liked") { value(false) }
            }
    }

    @Test
    fun `비로그인 사용자는 상품별 좋아요 여부를 모두 false로 조회할 수 있다`() {
        val firstProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 30))
        val secondProduct = productJpaRepository.save(ProductFixture.createSingleOption(index = 40))

        mockMvc
            .get("/api/products/likes") {
                param("productIds", firstProduct.id.toString(), secondProduct.id.toString())
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].productId") { value(firstProduct.id) }
                jsonPath("$.data[0].liked") { value(false) }
                jsonPath("$.data[1].productId") { value(secondProduct.id) }
                jsonPath("$.data[1].liked") { value(false) }
            }
    }

    @Test
    fun `인증된 회원은 상품 좋아요를 등록할 수 있다`() {
        val product = productJpaRepository.save(ProductFixture.createSingleOption(index = 1))

        mockMvc
            .post("/api/products/${product.id}/likes") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val savedMemberLike = memberLikeJpaRepository.findByMemberIdAndTargetIdAndLikeTargetType(1L, product.id, LikeTargetType.PRODUCT)
        savedMemberLike.shouldNotBeNull()
        savedMemberLike.deletedAt.shouldBeNull()
    }

    @Test
    fun `인증된 회원은 상품 좋아요를 취소할 수 있다`() {
        val product = productJpaRepository.save(ProductFixture.createSingleOption(index = 2))
        memberLikeJpaRepository.save(
            MemberLikeJpaEntity.from(
                MemberLike.create(memberId = 1L, targetId = product.id, likeTargetType = LikeTargetType.PRODUCT),
            ),
        )

        mockMvc
            .post("/api/products/${product.id}/likes/cancel") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, memberId = 1L))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val savedMemberLike = memberLikeJpaRepository.findByMemberIdAndTargetIdAndLikeTargetType(1L, product.id, LikeTargetType.PRODUCT)
        savedMemberLike.shouldNotBeNull()
        savedMemberLike.deletedAt.shouldNotBeNull()
    }
}

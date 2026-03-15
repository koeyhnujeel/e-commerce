package zoonza.commerce.adapter.`in`.like

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.adapter.out.persistence.like.LikeJpaEntity
import zoonza.commerce.adapter.out.persistence.like.LikeJpaRepository
import zoonza.commerce.adapter.out.persistence.member.MemberJapRepository
import zoonza.commerce.adapter.out.persistence.product.ProductJpaEntity
import zoonza.commerce.adapter.out.persistence.product.ProductJpaRepository
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.common.Email
import zoonza.commerce.common.Money
import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType
import zoonza.commerce.member.Member
import zoonza.commerce.member.Role
import zoonza.commerce.product.Product
import zoonza.commerce.product.ProductImage
import zoonza.commerce.product.ProductOption
import zoonza.commerce.support.MySqlTestContainerConfig
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class LikeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberJpaRepository: MemberJapRepository

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var likeJpaRepository: LikeJpaRepository

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    private val passwordEncoder = BCryptPasswordEncoder()

    @Test
    fun `토큰 없이 상품 좋아요 등록 요청 시 인증 필요 응답을 반환한다`() {
        mockMvc
            .post("/api/products/1/likes")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("인증이 필요합니다.") }
            }
    }

    @Test
    fun `토큰 없이 상품 좋아요 취소 요청 시 인증 필요 응답을 반환한다`() {
        mockMvc
            .post("/api/products/1/likes/cancel")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("인증이 필요합니다.") }
            }
    }

    @Test
    fun `상품 좋아요를 등록하면 like row를 저장한다`() {
        val member = insertMember()
        val product = insertProduct()

        mockMvc
            .post("/api/products/${product.id}/likes") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { doesNotExist() }
                jsonPath("$.error") { doesNotExist() }
            }

        val savedLike =
            likeJpaRepository.findByMemberIdAndTargetTypeAndTargetId(
                member.id,
                LikeTargetType.PRODUCT,
                product.id,
            )

        savedLike.shouldNotBeNull()
        savedLike.deletedAt shouldBe null
    }

    @Test
    fun `같은 상품 좋아요를 두 번 요청해도 row 하나만 유지한다`() {
        val member = insertMember()
        val product = insertProduct()

        mockMvc.post("/api/products/${product.id}/likes") {
            header("Authorization", bearerTokenOf(member))
        }
        mockMvc.post("/api/products/${product.id}/likes") {
            header("Authorization", bearerTokenOf(member))
        }

        likeJpaRepository.countByMemberIdAndTargetTypeAndTargetId(
            member.id,
            LikeTargetType.PRODUCT,
            product.id,
        ) shouldBe 1L
    }

    @Test
    fun `상품 좋아요를 취소하면 삭제 시각을 기록한다`() {
        val member = insertMember()
        val product = insertProduct()
        val like = insertLike(member.id, product.id)

        mockMvc
            .post("/api/products/${product.id}/likes/cancel") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val canceledLike =
            likeJpaRepository.findByMemberIdAndTargetTypeAndTargetId(
                member.id,
                LikeTargetType.PRODUCT,
                product.id,
            )

        canceledLike.shouldNotBeNull()
        canceledLike.id shouldBe like.id
        canceledLike.deletedAt.shouldNotBeNull()
    }

    @Test
    fun `이미 취소된 좋아요를 다시 취소해도 성공한다`() {
        val member = insertMember()
        val product = insertProduct()

        insertLike(
            memberId = member.id,
            productId = product.id,
            deletedAt = LocalDateTime.of(2026, 3, 15, 13, 0),
        )

        mockMvc
            .post("/api/products/${product.id}/likes/cancel") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val canceledLike =
            likeJpaRepository.findByMemberIdAndTargetTypeAndTargetId(
                member.id,
                LikeTargetType.PRODUCT,
                product.id,
            )

        canceledLike.shouldNotBeNull()
        canceledLike.deletedAt.shouldNotBeNull()
    }

    @Test
    fun `취소된 좋아요를 다시 등록하면 기존 row를 복구한다`() {
        val member = insertMember()
        val product = insertProduct()
        val originalLikedAt = LocalDateTime.of(2026, 3, 15, 12, 0)
        val deletedLike =
            insertLike(
                memberId = member.id,
                productId = product.id,
                likedAt = originalLikedAt,
                deletedAt = LocalDateTime.of(2026, 3, 15, 13, 0),
            )

        mockMvc
            .post("/api/products/${product.id}/likes") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val restoredLike =
            likeJpaRepository.findByMemberIdAndTargetTypeAndTargetId(
                member.id,
                LikeTargetType.PRODUCT,
                product.id,
            )

        restoredLike.shouldNotBeNull()
        restoredLike.id shouldBe deletedLike.id
        restoredLike.deletedAt shouldBe null
        restoredLike.likedAt shouldNotBe originalLikedAt
    }

    @Test
    fun `존재하지 않는 상품 좋아요 등록 요청이면 404를 반환한다`() {
        val member = insertMember()

        mockMvc
            .post("/api/products/999999/likes") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("NOT_FOUND") }
                jsonPath("$.error.message") { value("상품을 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `존재하지 않는 상품 좋아요 취소 요청이면 404를 반환한다`() {
        val member = insertMember()

        mockMvc
            .post("/api/products/999999/likes/cancel") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("NOT_FOUND") }
                jsonPath("$.error.message") { value("상품을 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `상품 ID가 0 이하이면 존재하지 않는 상품 응답을 반환한다`() {
        val member = insertMember()

        mockMvc
            .post("/api/products/0/likes") {
                header("Authorization", bearerTokenOf(member))
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("NOT_FOUND") }
                jsonPath("$.error.message") { value("상품을 찾을 수 없습니다.") }
            }
    }

    private fun bearerTokenOf(member: Member): String {
        return "Bearer ${tokenProvider.generateAccessToken(member.id, member.email.address, member.role).token}"
    }

    private fun insertMember(
        email: String = "member-${System.nanoTime()}@example.com",
    ): Member {
        return memberJpaRepository.save(
            Member.create(
                email = Email(email),
                passwordHash = passwordEncoder.encode("Password123!"),
                name = "홍길동",
                nickname = "tester-${System.nanoTime()}",
                phoneNumber = "010${(10000000..99999999).random()}",
                role = Role.CUSTOMER,
                registeredAt = LocalDateTime.now(),
            ),
        )
    }

    private fun insertProduct(): ProductJpaEntity {
        val product =
            Product.create(
                brandId = 10L,
                name = "오버핏 셔츠",
                description = "코튼 소재의 기본 셔츠",
                basePrice = Money(39_000L),
                categoryIds = listOf(100L, 200L),
                images =
                    listOf(
                        ProductImage.create(
                            imageUrl = "https://cdn.example.com/products/1/main.jpg",
                            isPrimary = true,
                            sortOrder = 0,
                        ),
                        ProductImage.create(
                            imageUrl = "https://cdn.example.com/products/1/detail.jpg",
                            isPrimary = false,
                            sortOrder = 1,
                        ),
                    ),
                options =
                    listOf(
                        ProductOption.create(
                            color = "화이트",
                            size = "S",
                            stockId = 1001L,
                        ),
                        ProductOption.create(
                            color = "블랙",
                            size = "M",
                            stockId = 1002L,
                        ),
                    ),
            )

        return productJpaRepository.save(ProductJpaEntity.from(product))
    }

    private fun insertLike(
        memberId: Long,
        productId: Long,
        likedAt: LocalDateTime = LocalDateTime.of(2026, 3, 15, 12, 0),
        deletedAt: LocalDateTime? = null,
    ): LikeJpaEntity {
        return likeJpaRepository.save(
            LikeJpaEntity.from(
                Like.create(
                    memberId = memberId,
                    targetType = LikeTargetType.PRODUCT,
                    targetId = productId,
                    likedAt = likedAt,
                    deletedAt = deletedAt,
                ),
            ),
        )
    }
}

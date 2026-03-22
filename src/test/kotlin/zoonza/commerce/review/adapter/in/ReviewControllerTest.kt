package zoonza.commerce.review.adapter.`in`

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.adapter.out.persistence.ProductJpaRepository
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.order.adapter.out.persistence.OrderJpaRepository
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.review.adapter.`in`.request.CreateReviewRequest
import zoonza.commerce.review.adapter.`in`.request.UpdateReviewRequest
import zoonza.commerce.review.adapter.out.persistence.ReviewJpaRepository
import zoonza.commerce.review.domain.Review
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.AuthFixture
import zoonza.commerce.support.fixture.MemberFixture
import zoonza.commerce.support.fixture.OrderFixture
import zoonza.commerce.support.fixture.ProductFixture
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class ReviewControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var accessTokenProvider: AccessTokenProvider

    @Autowired
    private lateinit var memberJapRepository: MemberJapRepository

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var orderJpaRepository: OrderJpaRepository

    @Autowired
    private lateinit var reviewJpaRepository: ReviewJpaRepository

    @Test
    fun `인증된 회원은 가장 최근 구매 확정 주문상품을 근거로 리뷰를 등록할 수 있다`() {
        val member =
            memberJapRepository.save(MemberFixture.createIndexed(index = 1))
        val product =
            productJpaRepository.save(ProductFixture.createSingleOption(index = 1))
        orderJpaRepository.save(
            OrderFixture.createPurchaseConfirmed(
                memberId = member.id,
                product = product,
                orderNumber = "ORD-REVIEW-${member.id}-2026-03-20",
                deliveredAt = LocalDateTime.of(2026, 3, 20, 9, 0),
                confirmedAt = LocalDateTime.of(2026, 3, 21, 9, 0),
            ),
        )
        val latestOrder =
            orderJpaRepository.save(
                OrderFixture.createPurchaseConfirmed(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-REVIEW-${member.id}-2026-03-22",
                    deliveredAt = LocalDateTime.of(2026, 3, 22, 9, 0),
                    confirmedAt = LocalDateTime.of(2026, 3, 23, 9, 0),
                ),
            )
        val latestOrderItemId = latestOrder.items.single().id

        mockMvc
            .post("/api/products/${product.id}/reviews") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateReviewRequest(
                        rating = 5,
                        content = "만족합니다.",
                    ),
                )
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { isNumber() }
            }

        val savedReview = reviewJpaRepository.findByMemberIdAndProductId(member.id, product.id)
        savedReview.shouldNotBeNull()
        savedReview.orderItemId shouldBe latestOrderItemId
        savedReview.optionColor shouldBe "BLACK"
        savedReview.optionSize shouldBe "M"
        savedReview.deletedAt.shouldBeNull()
    }

    @Test
    fun `비인증 사용자는 리뷰를 등록할 수 없다`() {
        val product = productJpaRepository.save(ProductFixture.createSingleOption(index = 1))

        mockMvc
            .post("/api/products/${product.id}/reviews") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    CreateReviewRequest(
                        rating = 5,
                        content = "리뷰",
                    ),
                )
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("인증이 필요합니다.") }
            }
    }

    @Test
    fun `상품 리뷰 목록은 공개 조회되고 삭제된 리뷰는 제외된다`() {
        val product = productJpaRepository.save(ProductFixture.createSingleOption(index = 1))
        val writer1 = memberJapRepository.save(MemberFixture.createIndexed(index = 1))
        val writer2 = memberJapRepository.save(MemberFixture.createIndexed(index = 2))
        val deletedWriter = memberJapRepository.save(MemberFixture.createIndexed(index = 3))
        val orderItemId1 =
            orderJpaRepository.save(
                OrderFixture.createPurchaseConfirmed(
                    memberId = writer1.id,
                    product = product,
                    orderNumber = "ORD-REVIEW-${writer1.id}-2026-03-20",
                    deliveredAt = LocalDateTime.of(2026, 3, 20, 9, 0),
                    confirmedAt = LocalDateTime.of(2026, 3, 21, 9, 0),
                ),
            ).items.single().id
        val orderItemId2 =
            orderJpaRepository.save(
                OrderFixture.createPurchaseConfirmed(
                    memberId = writer2.id,
                    product = product,
                    orderNumber = "ORD-REVIEW-${writer2.id}-2026-03-21",
                    deliveredAt = LocalDateTime.of(2026, 3, 21, 9, 0),
                    confirmedAt = LocalDateTime.of(2026, 3, 22, 9, 0),
                ),
            ).items.single().id
        val deletedOrderItemId =
            orderJpaRepository.save(
                OrderFixture.createPurchaseConfirmed(
                    memberId = deletedWriter.id,
                    product = product,
                    orderNumber = "ORD-REVIEW-${deletedWriter.id}-2026-03-19",
                    deliveredAt = LocalDateTime.of(2026, 3, 19, 9, 0),
                    confirmedAt = LocalDateTime.of(2026, 3, 20, 9, 0),
                ),
            ).items.single().id

        reviewJpaRepository.save(
            Review.create(
                memberId = writer1.id,
                productId = product.id,
                orderItemId = orderItemId1,
                optionColor = "BLACK",
                optionSize = "M",
                rating = 4,
                content = "첫 리뷰",
                createdAt = LocalDateTime.of(2026, 3, 20, 10, 0),
            ),
        )
        reviewJpaRepository.save(
            Review.create(
                memberId = writer2.id,
                productId = product.id,
                orderItemId = orderItemId2,
                optionColor = "BLACK",
                optionSize = "M",
                rating = 5,
                content = "최신 리뷰",
                createdAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            ),
        )
        val deletedReview =
            reviewJpaRepository.save(
                Review.create(
                    memberId = deletedWriter.id,
                    productId = product.id,
                    orderItemId = deletedOrderItemId,
                    optionColor = "BLACK",
                    optionSize = "M",
                    rating = 3,
                    content = "삭제될 리뷰",
                    createdAt = LocalDateTime.of(2026, 3, 20, 11, 0),
                ),
            )
        deletedReview.delete(LocalDateTime.of(2026, 3, 22, 9, 0))

        mockMvc
            .get("/api/products/${product.id}/reviews") {
                param("page", "1")
                param("size", "1")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.page") { value(1) }
                jsonPath("$.data.totalElements") { value(2) }
                jsonPath("$.data.totalPages") { value(2) }
                jsonPath("$.data.items.length()") { value(1) }
                jsonPath("$.data.items[0].content") { value("최신 리뷰") }
                jsonPath("$.data.items[0].authorNickname") { value(writer2.nickname) }
                jsonPath("$.data.items[0].optionColor") { value("BLACK") }
                jsonPath("$.data.items[0].optionSize") { value("M") }
            }
    }

    @Test
    fun `인증된 회원은 내 리뷰를 조회하고 수정하고 삭제할 수 있다`() {
        val member = memberJapRepository.save(MemberFixture.createIndexed(index = 1))
        val product = productJpaRepository.save(ProductFixture.createSingleOption(index = 1))
        val orderItemId =
            orderJpaRepository.save(
                OrderFixture.createPurchaseConfirmed(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-REVIEW-${member.id}-2026-03-21",
                    deliveredAt = LocalDateTime.of(2026, 3, 21, 9, 0),
                    confirmedAt = LocalDateTime.of(2026, 3, 22, 9, 0),
                ),
            ).items.single().id
        reviewJpaRepository.save(
            Review.create(
                memberId = member.id,
                productId = product.id,
                orderItemId = orderItemId,
                optionColor = "BLACK",
                optionSize = "M",
                rating = 5,
                content = "원본 리뷰",
                createdAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            ),
        )

        mockMvc
            .get("/api/products/${product.id}/reviews/me") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.authorNickname") { value(member.nickname) }
                jsonPath("$.data.optionColor") { value("BLACK") }
                jsonPath("$.data.optionSize") { value("M") }
                jsonPath("$.data.content") { value("원본 리뷰") }
            }

        mockMvc
            .put("/api/products/${product.id}/reviews/me") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    UpdateReviewRequest(
                        rating = 4,
                        content = "수정된 리뷰",
                    ),
                )
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val updatedReview = reviewJpaRepository.findByMemberIdAndProductId(member.id, product.id)
        updatedReview.shouldNotBeNull()
        updatedReview.rating shouldBe 4
        updatedReview.content shouldBe "수정된 리뷰"

        mockMvc
            .delete("/api/products/${product.id}/reviews/me") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }

        val deletedReview = reviewJpaRepository.findByMemberIdAndProductId(member.id, product.id)
        deletedReview.shouldNotBeNull()
        deletedReview.deletedAt.shouldNotBeNull()
    }

    @Test
    fun `삭제한 리뷰는 다시 작성하면 기존 리뷰를 복원한다`() {
        val member = memberJapRepository.save(MemberFixture.createIndexed(index = 1))
        val product = productJpaRepository.save(ProductFixture.createSingleOption(index = 1))
        val order =
            orderJpaRepository.save(
                OrderFixture.createPurchaseConfirmed(
                    memberId = member.id,
                    product = product,
                    orderNumber = "ORD-REVIEW-${member.id}-2026-03-21",
                    deliveredAt = LocalDateTime.of(2026, 3, 21, 9, 0),
                    confirmedAt = LocalDateTime.of(2026, 3, 22, 9, 0),
                ),
            )
        val orderItemId = order.items.single().id

        mockMvc
            .post("/api/products/${product.id}/reviews") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(CreateReviewRequest(rating = 5, content = "첫 리뷰"))
            }.andExpect {
                status { isOk() }
            }

        val firstReview = reviewJpaRepository.findByMemberIdAndProductId(member.id, product.id)
        firstReview.shouldNotBeNull()

        mockMvc
            .delete("/api/products/${product.id}/reviews/me") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
            }.andExpect {
                status { isOk() }
            }

        mockMvc
            .post("/api/products/${product.id}/reviews") {
                header(HttpHeaders.AUTHORIZATION, AuthFixture.authorizationHeader(accessTokenProvider, member))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(CreateReviewRequest(rating = 3, content = "다시 작성"))
            }.andExpect {
                status { isOk() }
            }

        val restoredReview = reviewJpaRepository.findByMemberIdAndProductId(member.id, product.id)
        restoredReview.shouldNotBeNull()
        restoredReview.id shouldBe firstReview.id
        restoredReview.orderItemId shouldBe orderItemId
        restoredReview.optionColor shouldBe "BLACK"
        restoredReview.optionSize shouldBe "M"
        restoredReview.content shouldBe "다시 작성"
        restoredReview.rating shouldBe 3
        restoredReview.deletedAt.shouldBeNull()
        reviewJpaRepository.count() shouldBe 1L
    }
}

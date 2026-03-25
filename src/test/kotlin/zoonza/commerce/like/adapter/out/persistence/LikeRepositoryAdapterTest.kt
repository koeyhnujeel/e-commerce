package zoonza.commerce.like.adapter.out.persistence

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.like.application.port.out.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.support.MySqlTestContainerConfig

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class LikeRepositoryAdapterTest {
    @Autowired
    private lateinit var likeRepository: LikeRepository

    @Autowired
    private lateinit var memberLikeJpaRepository: MemberLikeJpaRepository

    @Test
    fun `회원별 좋아요 대상 조회는 활성 좋아요만 반환한다`() {
        memberLikeJpaRepository.save(MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT))
        memberLikeJpaRepository.save(MemberLike.create(memberId = 1L, targetId = 20L, targetType = LikeTargetType.PRODUCT).apply { cancel() })
        memberLikeJpaRepository.save(MemberLike.create(memberId = 2L, targetId = 30L, targetType = LikeTargetType.PRODUCT))

        val result = likeRepository.findActiveTargetIds(
            memberId = 1L,
            targetIds = listOf(10L, 20L, 30L),
            targetType = LikeTargetType.PRODUCT,
        )

        result shouldBe setOf(10L)
    }
}

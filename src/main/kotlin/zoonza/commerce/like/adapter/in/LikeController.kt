package zoonza.commerce.like.adapter.`in`

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.support.web.ApiResponse
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.security.CurrentMemberInfo

@RestController
@RequestMapping("/api/products")
class LikeController(
    private val likeService: LikeService,
) {
    @PostMapping("/{productId}/likes")
    fun likeProduct(
        @CurrentMember currentMember: CurrentMemberInfo,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        likeService.like(
            memberId = currentMember.memberId,
            targetId = productId,
            targetType = LikeTargetType.PRODUCT,
        )

        return ApiResponse.success()
    }

    @PostMapping("/{productId}/likes/cancel")
    fun cancelProductLike(
        @CurrentMember currentMember: CurrentMemberInfo,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        likeService.cancelLike(
            memberId = currentMember.memberId,
            targetId = productId,
            targetType = LikeTargetType.PRODUCT,
        )

        return ApiResponse.success()
    }
}

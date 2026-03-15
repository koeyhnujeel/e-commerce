package zoonza.commerce.adapter.`in`.like

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.adapter.`in`.auth.AuthenticatedMember
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.like.LikeTargetType
import zoonza.commerce.like.port.`in`.LikeService

@RestController
@RequestMapping("/api/products")
class LikeController(
    private val likeService: LikeService,
) {
    @PostMapping("/{productId}/likes")
    fun like(
        @AuthenticationPrincipal authenticatedMember: AuthenticatedMember,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        likeService.like(
            memberId = authenticatedMember.memberId,
            targetType = LikeTargetType.PRODUCT,
            targetId = productId,
        )

        return ApiResponse.success()
    }

    @PostMapping("/{productId}/likes/cancel")
    fun cancel(
        @AuthenticationPrincipal authenticatedMember: AuthenticatedMember,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        likeService.cancel(
            memberId = authenticatedMember.memberId,
            targetType = LikeTargetType.PRODUCT,
            targetId = productId,
        )

        return ApiResponse.success()
    }
}

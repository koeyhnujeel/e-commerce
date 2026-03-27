package zoonza.commerce.like.adapter.`in`

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.web.ApiResponse

@RestController
@RequestMapping("/api/products")
class LikeController(
    private val likeService: LikeService,
) {
    @PostMapping("/{productId}/likes")
    fun likeProduct(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        likeService.likeProduct(
            memberId = currentMember.memberId,
            targetId = productId,
        )

        return ApiResponse.success()
    }

    @PostMapping("/{productId}/likes/cancel")
    fun cancelProductLike(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        likeService.unlikeProduct(
            memberId = currentMember.memberId,
            targetId = productId,
        )

        return ApiResponse.success()
    }
}

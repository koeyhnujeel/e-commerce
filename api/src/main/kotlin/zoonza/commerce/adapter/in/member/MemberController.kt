package zoonza.commerce.adapter.`in`.member

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.adapter.`in`.member.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.member.port.`in`.MemberService

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
) {
    @PostMapping("/signup/email-verifications")
    fun sendSignupEmailVerificationCode(
        @Valid @RequestBody request: SendSignupEmailVerificationCodeRequest,
    ): ApiResponse<Nothing> {
        memberService.sendSignupEmailVerificationCode(request.email)

        return ApiResponse.success()
    }
}

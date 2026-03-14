package zoonza.commerce.adapter.`in`.member

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.adapter.`in`.member.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.adapter.`in`.member.request.SignupMemberRequest
import zoonza.commerce.adapter.`in`.member.request.VerifySignupEmailVerificationCodeRequest
import zoonza.commerce.adapter.`in`.member.response.SignupMemberResponse
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.member.dto.SignupCommand
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

    @PostMapping("/signup/email-verifications/verify")
    fun verifySignupEmailVerificationCode(
        @Valid @RequestBody request: VerifySignupEmailVerificationCodeRequest,
    ): ApiResponse<Nothing> {
        memberService.verifySignupEmailCode(
            email = request.email,
            code = request.code,
        )

        return ApiResponse.success()
    }

    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignupMemberRequest,
    ): ApiResponse<SignupMemberResponse> {
        val memberId =
            memberService.signup(
                SignupCommand(
                    email = request.email,
                    password = request.password,
                    name = request.name,
                    phoneNumber = request.phoneNumber,
                ),
            )

        return ApiResponse.success(SignupMemberResponse(id = memberId))
    }
}

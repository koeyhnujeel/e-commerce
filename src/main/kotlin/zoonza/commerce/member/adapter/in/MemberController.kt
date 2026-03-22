package zoonza.commerce.member.adapter.`in`

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.support.web.ApiResponse
import zoonza.commerce.member.adapter.`in`.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.member.adapter.`in`.request.SignupMemberRequest
import zoonza.commerce.member.adapter.`in`.request.VerifySignupEmailVerificationCodeRequest
import zoonza.commerce.member.adapter.`in`.response.SignupMemberResponse
import zoonza.commerce.member.application.dto.SignupCommand
import zoonza.commerce.member.application.port.`in`.MemberService

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
        val command = SignupCommand(
            email = request.email,
            password = request.password,
            name = request.name,
            phoneNumber = request.phoneNumber,
        )

        val memberId = memberService.signup(command)

        return ApiResponse.success(SignupMemberResponse(id = memberId))
    }
}

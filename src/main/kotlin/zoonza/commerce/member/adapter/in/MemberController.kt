package zoonza.commerce.member.adapter.`in`

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.member.adapter.`in`.request.CreateMemberAddressRequest
import zoonza.commerce.member.adapter.`in`.request.SendSignupEmailVerificationCodeRequest
import zoonza.commerce.member.adapter.`in`.request.SignupMemberRequest
import zoonza.commerce.member.adapter.`in`.request.UpdateMemberAddressRequest
import zoonza.commerce.member.adapter.`in`.request.VerifySignupEmailVerificationCodeRequest
import zoonza.commerce.member.adapter.`in`.response.CreateMemberAddressResponse
import zoonza.commerce.member.adapter.`in`.response.MemberAddressResponse
import zoonza.commerce.member.adapter.`in`.response.SignupMemberResponse
import zoonza.commerce.member.application.dto.CreateMemberAddressCommand
import zoonza.commerce.member.application.dto.SignupCommand
import zoonza.commerce.member.application.dto.UpdateMemberAddressCommand
import zoonza.commerce.member.application.port.`in`.MemberService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.web.ApiResponse

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
        val command = SignupCommand.of(
            email = request.email,
            password = request.password,
            name = request.name,
            phoneNumber = request.phoneNumber,
        )

        val memberId = memberService.signup(command)

        return ApiResponse.success(SignupMemberResponse.of(memberId))
    }

    @GetMapping("/me/addresses")
    fun getMyAddresses(
        @AuthenticationPrincipal currentMember: CurrentMember,
    ): ApiResponse<List<MemberAddressResponse>> {
        return ApiResponse.success(
            memberService.getMyAddresses(currentMember.memberId).map(MemberAddressResponse::from),
        )
    }

    @PostMapping("/me/addresses")
    fun addAddress(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @Valid @RequestBody request: CreateMemberAddressRequest,
    ): ApiResponse<CreateMemberAddressResponse> {
        val addressId =
            memberService.addAddress(
                memberId = currentMember.memberId,
                command = CreateMemberAddressCommand.of(
                    label = request.label,
                    recipientName = request.recipientName,
                    recipientPhoneNumber = request.recipientPhoneNumber,
                    zipCode = request.zipCode,
                    baseAddress = request.baseAddress,
                    detailAddress = request.detailAddress,
                    isDefault = request.isDefault,
                ),
            )

        return ApiResponse.success(CreateMemberAddressResponse.of(addressId))
    }

    @PatchMapping("/me/addresses/{addressId}")
    fun updateAddress(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable addressId: Long,
        @Valid @RequestBody request: UpdateMemberAddressRequest,
    ): ApiResponse<Nothing> {
        memberService.updateAddress(
            memberId = currentMember.memberId,
            addressId = addressId,
            command = UpdateMemberAddressCommand.of(
                label = request.label,
                recipientName = request.recipientName,
                recipientPhoneNumber = request.recipientPhoneNumber,
                zipCode = request.zipCode,
                baseAddress = request.baseAddress,
                detailAddress = request.detailAddress,
                isDefault = request.isDefault,
            ),
        )
        return ApiResponse.success()
    }

    @DeleteMapping("/me/addresses/{addressId}")
    fun removeAddress(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable addressId: Long,
    ): ApiResponse<Nothing> {
        memberService.removeAddress(currentMember.memberId, addressId)
        return ApiResponse.success()
    }

    @PostMapping("/me/addresses/{addressId}/default")
    fun changeDefaultAddress(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable addressId: Long,
    ): ApiResponse<Nothing> {
        memberService.changeDefaultAddress(currentMember.memberId, addressId)
        return ApiResponse.success()
    }
}

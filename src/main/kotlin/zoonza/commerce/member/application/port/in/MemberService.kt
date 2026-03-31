package zoonza.commerce.member.application.port.`in`

import zoonza.commerce.member.MemberAddressSnapshot
import zoonza.commerce.member.application.dto.CreateMemberAddressCommand
import zoonza.commerce.member.application.dto.SignupCommand
import zoonza.commerce.member.application.dto.UpdateMemberAddressCommand

interface MemberService {
    fun sendSignupEmailVerificationCode(email: String)

    fun verifySignupEmailCode(email: String, code: String)

    fun signup(command: SignupCommand): Long

    fun getMyAddresses(memberId: Long): List<MemberAddressSnapshot>

    fun addAddress(
        memberId: Long,
        command: CreateMemberAddressCommand,
    ): Long

    fun updateAddress(
        memberId: Long,
        addressId: Long,
        command: UpdateMemberAddressCommand,
    )

    fun removeAddress(
        memberId: Long,
        addressId: Long,
    )

    fun changeDefaultAddress(
        memberId: Long,
        addressId: Long,
    )
}

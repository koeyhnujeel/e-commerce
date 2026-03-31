package zoonza.commerce.member.domain

import zoonza.commerce.shared.Email
import java.time.LocalDateTime

class Member(
    val id: Long = 0,
    val email: Email,
    val passwordHash: String,
    val name: String,
    val nickname: String,
    val phoneNumber: String,
    val role: Role = Role.CUSTOMER,
    val registeredAt: LocalDateTime,
    var lastLoginAt: LocalDateTime? = null,
    val addresses: MutableList<MemberAddress> = mutableListOf(),
) {
    companion object {
        fun create(
            email: Email,
            passwordHash: String,
            name: String,
            nickname: String,
            phoneNumber: String,
            role: Role = Role.CUSTOMER,
            registeredAt: LocalDateTime,
            lastLoginAt: LocalDateTime? = null,
        ): Member {
            return Member(
                email = email,
                passwordHash = passwordHash,
                name = name,
                nickname = nickname,
                phoneNumber = phoneNumber,
                role = role,
                registeredAt = registeredAt,
                lastLoginAt = lastLoginAt,
            )
        }
    }

    fun recordLogin(lastLoginAt: LocalDateTime) {
        this.lastLoginAt = lastLoginAt
    }

    fun verifyPassword(
        password: String,
        passwordEncoder: PasswordEncoder,
    ): Boolean {
        return passwordEncoder.matches(password, this.passwordHash)
    }

    fun addAddress(address: MemberAddress) {
        if (addresses.isEmpty() || address.isDefault) {
            clearDefaultAddress()
            address.markDefault()
        }

        addresses.add(address)
        ensureSingleDefaultAddress()
    }

    fun updateAddress(
        addressId: Long,
        label: String,
        recipientName: String,
        recipientPhoneNumber: String,
        zipCode: String,
        baseAddress: String,
        detailAddress: String,
        isDefault: Boolean,
    ) {
        val address = findAddress(addressId)
        address.update(
            label = label,
            recipientName = recipientName,
            recipientPhoneNumber = recipientPhoneNumber,
            zipCode = zipCode,
            baseAddress = baseAddress,
            detailAddress = detailAddress,
            isDefault = isDefault,
        )

        if (address.isDefault) {
            addresses.filter { it.id != addressId }.forEach(MemberAddress::unmarkDefault)
        } else if (addresses.none(MemberAddress::isDefault)) {
            address.markDefault()
        }

        ensureSingleDefaultAddress()
    }

    fun removeAddress(addressId: Long) {
        val removed = findAddress(addressId)
        addresses.removeIf { it.id == addressId }

        if (removed.isDefault && addresses.isNotEmpty()) {
            addresses.first().markDefault()
        }

        ensureSingleDefaultAddress()
    }

    fun changeDefaultAddress(addressId: Long) {
        val address = findAddress(addressId)
        clearDefaultAddress()
        address.markDefault()
        ensureSingleDefaultAddress()
    }

    fun findAddress(addressId: Long): MemberAddress {
        return addresses.firstOrNull { it.id == addressId }
            ?: throw IllegalArgumentException("배송지를 찾을 수 없습니다.")
    }

    fun defaultAddress(): MemberAddress {
        return addresses.firstOrNull(MemberAddress::isDefault)
            ?: throw IllegalArgumentException("기본 배송지를 찾을 수 없습니다.")
    }

    private fun clearDefaultAddress() {
        addresses.forEach(MemberAddress::unmarkDefault)
    }

    private fun ensureSingleDefaultAddress() {
        require(addresses.count(MemberAddress::isDefault) <= 1) { "기본 배송지는 하나만 설정할 수 있습니다." }
    }
}

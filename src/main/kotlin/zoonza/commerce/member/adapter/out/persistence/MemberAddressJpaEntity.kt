package zoonza.commerce.member.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import zoonza.commerce.member.domain.MemberAddress

@Entity
@Table(name = "member_address")
class MemberAddressJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    var label: String = "",

    @Column(name = "recipient_name", nullable = false)
    var recipientName: String = "",

    @Column(name = "recipient_phone_number", nullable = false)
    var recipientPhoneNumber: String = "",

    @Column(name = "zip_code", nullable = false)
    var zipCode: String = "",

    @Column(name = "base_address", nullable = false)
    var baseAddress: String = "",

    @Column(name = "detail_address", nullable = false)
    var detailAddress: String = "",

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,
) {
    companion object {
        fun from(address: MemberAddress): MemberAddressJpaEntity {
            return MemberAddressJpaEntity(
                id = address.id,
                label = address.label,
                recipientName = address.recipientName,
                recipientPhoneNumber = address.recipientPhoneNumber,
                zipCode = address.zipCode,
                baseAddress = address.baseAddress,
                detailAddress = address.detailAddress,
                isDefault = address.isDefault,
            )
        }
    }

    fun toDomain(): MemberAddress {
        return MemberAddress(
            id = id,
            label = label,
            recipientName = recipientName,
            recipientPhoneNumber = recipientPhoneNumber,
            zipCode = zipCode,
            baseAddress = baseAddress,
            detailAddress = detailAddress,
            isDefault = isDefault,
        )
    }

    fun updateFrom(address: MemberAddress) {
        label = address.label
        recipientName = address.recipientName
        recipientPhoneNumber = address.recipientPhoneNumber
        zipCode = address.zipCode
        baseAddress = address.baseAddress
        detailAddress = address.detailAddress
        isDefault = address.isDefault
    }
}

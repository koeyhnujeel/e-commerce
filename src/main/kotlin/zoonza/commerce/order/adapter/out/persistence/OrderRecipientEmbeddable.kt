package zoonza.commerce.order.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import zoonza.commerce.order.domain.OrderRecipient

@Embeddable
class OrderRecipientEmbeddable(
    @Column(name = "recipient_name", nullable = false)
    val recipientName: String = "",

    @Column(name = "recipient_phone_number", nullable = false)
    val recipientPhoneNumber: String = "",

    @Column(name = "zip_code", nullable = false)
    val zipCode: String = "",

    @Column(name = "base_address", nullable = false)
    val baseAddress: String = "",

    @Column(name = "detail_address", nullable = false)
    val detailAddress: String = "",
) {
    companion object {
        fun from(recipient: OrderRecipient): OrderRecipientEmbeddable {
            return OrderRecipientEmbeddable(
                recipientName = recipient.recipientName,
                recipientPhoneNumber = recipient.recipientPhoneNumber,
                zipCode = recipient.zipCode,
                baseAddress = recipient.baseAddress,
                detailAddress = recipient.detailAddress,
            )
        }
    }

    fun toDomain(): OrderRecipient {
        return OrderRecipient(
            recipientName = recipientName,
            recipientPhoneNumber = recipientPhoneNumber,
            zipCode = zipCode,
            baseAddress = baseAddress,
            detailAddress = detailAddress,
        )
    }
}

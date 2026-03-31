package zoonza.commerce.member.adapter.`in`.response

data class CreateMemberAddressResponse(
    val id: Long,
) {
    companion object {
        fun of(id: Long): CreateMemberAddressResponse {
            return CreateMemberAddressResponse(id)
        }
    }
}

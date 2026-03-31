package zoonza.commerce.member.adapter.`in`.response

data class SignupMemberResponse(
    val id: Long,
) {
    companion object {
        fun of(id: Long): SignupMemberResponse {
            return SignupMemberResponse(id)
        }
    }
}

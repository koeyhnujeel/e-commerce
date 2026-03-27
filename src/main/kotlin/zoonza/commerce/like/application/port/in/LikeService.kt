package zoonza.commerce.like.application.port.`in`

interface LikeService {
    fun likeProduct(memberId: Long, targetId: Long)

    fun unlikeProduct(memberId: Long, targetId: Long)
}

package zoonza.commerce.cart.adapter.out.persistence

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Repository
import zoonza.commerce.cart.domain.Cart
import zoonza.commerce.cart.domain.CartErrorCode
import zoonza.commerce.cart.domain.CartRepository
import zoonza.commerce.shared.BusinessException

@Repository
class CartRepositoryAdapter(
    private val cartJpaRepository: CartJpaRepository,
) : CartRepository {
    override fun findByMemberId(memberId: Long): Cart? {
        return cartJpaRepository.findByMemberId(memberId)?.toDomain()
    }

    override fun save(cart: Cart): Cart {
        val jpaEntity =
            if (cart.id == 0L) {
                CartJpaEntity.from(cart)
            } else {
                cartJpaRepository.findByIdOrNull(cart.id)
                    ?.also { existing ->
                        if (existing.version != cart.version) {
                            throw BusinessException(CartErrorCode.CONCURRENT_CART_MODIFICATION)
                        }
                        existing.updateFrom(cart)
                    }
                    ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
            }

        return try {
            cartJpaRepository.saveAndFlush(jpaEntity).toDomain()
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw BusinessException(CartErrorCode.CONCURRENT_CART_MODIFICATION)
        } catch (_: OptimisticLockingFailureException) {
            throw BusinessException(CartErrorCode.CONCURRENT_CART_MODIFICATION)
        }
    }
}

package zoonza.commerce.cart.application.service

import org.springframework.stereotype.Service
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.cart.CartApi
import zoonza.commerce.cart.CartOrderItem
import zoonza.commerce.cart.application.dto.CartItemUnavailableReason
import zoonza.commerce.cart.application.dto.CartItemView
import zoonza.commerce.cart.application.dto.CartSummaryView
import zoonza.commerce.cart.application.dto.CartView
import zoonza.commerce.cart.application.port.`in`.CartService
import zoonza.commerce.cart.domain.Cart
import zoonza.commerce.cart.domain.CartErrorCode
import zoonza.commerce.cart.domain.CartRepository
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.ProductOptionSummary
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.shared.BusinessException

@Service
class DefaultCartService(
    private val cartRepository: CartRepository,
    private val catalogApi: CatalogApi,
    private val inventoryApi: InventoryApi,
) : CartService, CartApi {
    @Transactional(readOnly = true)
    override fun getMyCart(memberId: Long): CartView {
        val cart = cartRepository.findByMemberId(memberId) ?: return CartView.empty()
        if (cart.items.isEmpty()) {
            return CartView.empty()
        }

        val optionIds = cart.items.map { it.productOptionId }.toSet()
        val optionSummaries = catalogApi.getProductOptionSummaries(optionIds)
        val availableQuantities = inventoryApi.getAvailableQuantities(optionIds)

        val itemViews =
            cart.items.map { item ->
                val optionSummary = optionSummaries[item.productOptionId]
                    ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
                val availableQuantity = availableQuantities[item.productOptionId] ?: 0L
                val unitPrice = optionSummary.basePrice + optionSummary.additionalPrice
                val unavailableReason = determineUnavailableReason(optionSummary, availableQuantity, item.quantity)
                val purchasable = unavailableReason == null

                CartItemView(
                    productId = item.productId,
                    productOptionId = optionSummary.productOptionId,
                    productName = optionSummary.productName,
                    primaryImageUrl = optionSummary.primaryImageUrl,
                    color = optionSummary.color,
                    size = optionSummary.size,
                    quantity = item.quantity,
                    unitPrice = unitPrice,
                    lineAmount = unitPrice * item.quantity,
                    purchasable = purchasable,
                    unavailableReason = unavailableReason,
                )
            }

        return CartView(
            items = itemViews,
            summary =
                CartSummaryView(
                    itemCount = itemViews.size,
                    totalQuantity = itemViews.sumOf { it.quantity },
                    totalAmount = itemViews.sumOf { it.lineAmount },
                    purchasableItemCount = itemViews.count { it.purchasable },
                    purchasableAmount = itemViews.filter { it.purchasable }.sumOf { it.lineAmount },
                ),
        )
    }

    @Transactional
    override fun addItem(
        memberId: Long,
        productId: Long,
        productOptionId: Long,
        quantity: Long,
    ) {
        catalogApi.validateAvailableProductOption(productId, productOptionId)

        try {
            val cart = cartRepository.findByMemberId(memberId) ?: Cart.create(memberId)
            cart.addItem(productId, productOptionId, quantity)
            cartRepository.save(cart)
        } catch (_: DataIntegrityViolationException) {
            val existingCart = cartRepository.findByMemberId(memberId)
                ?: throw BusinessException(CartErrorCode.CONCURRENT_CART_MODIFICATION)

            existingCart.addItem(productId, productOptionId, quantity)
            cartRepository.save(existingCart)
        }
    }

    @Transactional
    override fun changeItemQuantity(
        memberId: Long,
        productOptionId: Long,
        quantity: Long,
    ) {
        val cart = cartRepository.findByMemberId(memberId)
            ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
        val cartItem = cart.items.firstOrNull { it.productOptionId == productOptionId }
            ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)

        catalogApi.validateAvailableProductOption(cartItem.productId, productOptionId)

        cart.changeQuantity(productOptionId, quantity)
        cartRepository.save(cart)
    }

    @Transactional
    override fun removeItem(
        memberId: Long,
        productOptionId: Long,
    ) {
        val cart = cartRepository.findByMemberId(memberId)
            ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)

        cart.removeItem(productOptionId)
        cartRepository.save(cart)
    }

    @Transactional
    override fun clearCart(memberId: Long) {
        val cart = cartRepository.findByMemberId(memberId) ?: return
        cart.clear()
        cartRepository.save(cart)
    }

    @Transactional(readOnly = true)
    override fun getSelectedItems(
        memberId: Long,
        productOptionIds: Set<Long>,
    ): List<CartOrderItem> {
        require(productOptionIds.isNotEmpty()) { "주문할 장바구니 항목은 최소 1개 이상이어야 합니다." }

        val cart = cartRepository.findByMemberId(memberId)
            ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
        val selectedItems = cart.items.filter { it.productOptionId in productOptionIds }

        if (selectedItems.size != productOptionIds.size) {
            throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
        }

        return selectedItems.map { item ->
            CartOrderItem(
                productId = item.productId,
                productOptionId = item.productOptionId,
                quantity = item.quantity,
            )
        }
    }

    @Transactional
    override fun removeItems(
        memberId: Long,
        productOptionIds: Set<Long>,
    ) {
        if (productOptionIds.isEmpty()) {
            return
        }

        val cart = cartRepository.findByMemberId(memberId) ?: return
        productOptionIds.forEach(cart::removeItem)
        cartRepository.save(cart)
    }

    private fun determineUnavailableReason(
        optionSummary: ProductOptionSummary,
        availableQuantity: Long,
        requestedQuantity: Long,
    ): CartItemUnavailableReason? {
        if (!optionSummary.availableForSale) {
            return CartItemUnavailableReason.SALE_UNAVAILABLE
        }

        if (availableQuantity == 0L) {
            return CartItemUnavailableReason.OUT_OF_STOCK
        }

        if (availableQuantity < requestedQuantity) {
            return CartItemUnavailableReason.INSUFFICIENT_STOCK
        }

        return null
    }
}

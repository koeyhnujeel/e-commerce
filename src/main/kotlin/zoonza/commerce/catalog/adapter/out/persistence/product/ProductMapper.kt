package zoonza.commerce.catalog.adapter.out.persistence.product

import zoonza.commerce.catalog.domain.product.Product
import zoonza.commerce.catalog.domain.product.ProductImage
import zoonza.commerce.catalog.domain.product.ProductOption

object ProductMapper {
    fun toDomain(entity: ProductEntity): Product {
        return Product(
            id = entity.id,
            brandId = entity.brandId,
            name = entity.name,
            description = entity.description,
            basePrice = entity.basePrice,
            categoryId = entity.categoryId,
            images = entity.images.map(::toDomainImage).toMutableList(),
            options = entity.options.map(::toDomainOption).toMutableList(),
        )
    }

    fun toEntity(product: Product): ProductEntity {
        return ProductEntity(
            id = product.id,
            brandId = product.brandId,
            name = product.name,
            description = product.description,
            basePrice = product.basePrice,
            categoryId = product.categoryId,
            images = product.images.map(::toEntityImage).toMutableList(),
            options = product.options.map(::toEntityOption).toMutableList(),
        )
    }

    private fun toDomainImage(entity: ProductImageEntity): ProductImage {
        return ProductImage(
            id = entity.id,
            imageUrl = entity.imageUrl,
            isPrimary = entity.isPrimary,
            sortOrder = entity.sortOrder,
        )
    }

    private fun toDomainOption(entity: ProductOptionEntity): ProductOption {
        return ProductOption(
            id = entity.id,
            color = entity.color,
            size = entity.size,
            sortOder = entity.sortOrder,
            additionalPrice = entity.additionalPrice,
        )
    }

    private fun toEntityImage(image: ProductImage): ProductImageEntity {
        return ProductImageEntity(
            id = image.id,
            imageUrl = image.imageUrl,
            isPrimary = image.isPrimary,
            sortOrder = image.sortOrder,
        )
    }

    private fun toEntityOption(option: ProductOption): ProductOptionEntity {
        return ProductOptionEntity(
            id = option.id,
            color = option.color,
            size = option.size,
            sortOrder = option.sortOder,
            additionalPrice = option.additionalPrice,
        )
    }
}

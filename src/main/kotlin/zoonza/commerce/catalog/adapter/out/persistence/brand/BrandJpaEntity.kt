package zoonza.commerce.catalog.adapter.out.persistence.brand

import jakarta.persistence.*
import zoonza.commerce.catalog.domain.brand.Brand

@Entity
@Table(name = "brand")
class BrandJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",
) {
    companion object {
        fun from(brand: Brand): BrandJpaEntity {
            return BrandJpaEntity(
                id = brand.id,
                name = brand.name,
            )
        }
    }

    fun toDomain(): Brand {
        return Brand(
            id = id,
            name = name,
        )
    }
}

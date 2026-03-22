package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.catalog.domain.ProductOption

interface ProductOptionJpaRepository : JpaRepository<ProductOption, Long>

package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.catalog.domain.Product

interface ProductJpaRepository : JpaRepository<Product, Long>

package zoonza.commerce.catalog.adapter.out.persistence.product

import org.springframework.data.jpa.repository.JpaRepository
interface ProductJpaRepository : JpaRepository<ProductJpaEntity, Long>

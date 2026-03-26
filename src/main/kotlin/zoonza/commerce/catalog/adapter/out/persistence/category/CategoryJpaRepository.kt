package zoonza.commerce.catalog.adapter.out.persistence.category

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryJpaRepository : JpaRepository<CategoryJpaEntity, Long>

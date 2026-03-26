package zoonza.commerce.catalog.adapter.out.persistence.category

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.catalog.domain.category.Category

interface CategoryJpaRepository : JpaRepository<Category, Long>

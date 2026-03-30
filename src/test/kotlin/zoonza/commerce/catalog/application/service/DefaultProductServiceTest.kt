package zoonza.commerce.catalog.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.*
import zoonza.commerce.catalog.domain.category.CategoryRepository
import zoonza.commerce.catalog.domain.product.*
import zoonza.commerce.shared.Money
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

class DefaultProductServiceTest {
    private val productQueryRepository = mockk<ProductQueryRepository>()
    private val categoryRepository = mockk<CategoryRepository>()
    private val catalogService =
        DefaultProductService(
            productQueryRepository = productQueryRepository,
            categoryRepository = categoryRepository,
        )

    @Test
    fun `상품 목록 조회는 정렬과 좋아요 수를 함께 반환한다`() {
        val pageQuery = slot<PageQuery>()
        every { categoryRepository.findAllDescendantIds(1L) } returns linkedSetOf(1L, 2L)

        every {
            productQueryRepository.findPageByCategoryIds(
                categoryIds = linkedSetOf(1L, 2L),
                pageQuery = capture(pageQuery),
                sort = ProductListSort.PRICE_DESC,
            )
        } returns PageResult(
            items = listOf(
                ProductSummaryQueryResult(
                    productId = 20L,
                    name = "상품20",
                    brandName = "브랜드20",
                    primaryImageUrl = "https://cdn.example.com/product-20-primary.jpg",
                    basePrice = 39_900,
                    likeCount = 5L,
                    saleStatus = ProductSaleStatus.AVAILABLE,
                ),
                ProductSummaryQueryResult(
                    productId = 10L,
                    name = "상품10",
                    brandName = "브랜드10",
                    primaryImageUrl = "https://cdn.example.com/product-10-primary.jpg",
                    basePrice = 19_900,
                    likeCount = 2L,
                    saleStatus = ProductSaleStatus.AVAILABLE,
                ),
            ),
            page = 0,
            size = 20,
            totalElements = 2,
            totalPages = 1,
        )

        val result = catalogService.getCategoryProducts(
            page = 0,
            size = 20,
            categoryId = 1L,
            sort = ProductListSort.PRICE_DESC,
        )

        pageQuery.captured shouldBe PageQuery(page = 0, size = 20)
        result.items.map { it.productId } shouldBe listOf(20L, 10L)
        result.items.map { it.brandName } shouldBe listOf("브랜드20", "브랜드10")
        result.items.map { it.likeCount } shouldBe listOf(5L, 2L)
        result.items.map { it.saleStatus } shouldBe listOf(ProductSaleStatus.AVAILABLE, ProductSaleStatus.AVAILABLE)
    }

    @Test
    fun `상품 목록 조회는 로그인 여부와 무관하게 동일한 결과를 반환한다`() {
        val product = product(id = 10L, price = 19_900, categoryId = 1L)

        every { categoryRepository.findAllDescendantIds(1L) } returns linkedSetOf(1L)

        every {
            productQueryRepository.findPageByCategoryIds(
                categoryIds = linkedSetOf(1L),
                pageQuery = PageQuery(page = 0, size = 20),
                sort = ProductListSort.LATEST,
            )
        } returns PageResult(
            items = listOf(
                ProductSummaryQueryResult(
                    productId = 10L,
                    name = product.name,
                    brandName = "브랜드10",
                    primaryImageUrl = product.images.first { it.isPrimary }.imageUrl,
                    basePrice = product.basePrice.amount.longValueExact(),
                    likeCount = 3L,
                    saleStatus = ProductSaleStatus.AVAILABLE,
                ),
            ),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
        )

        val result = catalogService.getCategoryProducts(
            page = 0,
            size = 20,
            categoryId = 1L,
            sort = ProductListSort.LATEST,
        )

        result.items.single().productId shouldBe 10L
        result.items.single().brandName shouldBe "브랜드10"
        verify(exactly = 1) { categoryRepository.findAllDescendantIds(1L) }
    }

    @Test
    fun `상품 상세 조회는 이미지 옵션 좋아요 수를 함께 반환한다`() {
        every { productQueryRepository.findProductDetailsById(10L) } returns ProductDetailQueryResult(
            productId = 10L,
            name = "상품10",
            brandName = "브랜드10",
            description = "상품 설명10",
            basePrice = 19_900,
            categoryId = 10L,
            images = listOf(
                ProductImageQueryResult(
                    imageUrl = "https://cdn.example.com/product-10-primary.jpg",
                    isPrimary = true,
                    sortOrder = 0,
                ),
                ProductImageQueryResult(
                    imageUrl = "https://cdn.example.com/product-10-secondary.jpg",
                    isPrimary = false,
                    sortOrder = 1,
                ),
            ),
            options = listOf(
                ProductOptionQueryResult(
                    productOptionId = 101L,
                    color = "BLACK",
                    size = "M",
                    sortOrder = 0,
                    additionalPrice = 0L,
                ),
                ProductOptionQueryResult(
                    productOptionId = 102L,
                    color = "WHITE",
                    size = "L",
                    sortOrder = 1,
                    additionalPrice = 1_000L,
                ),
            ),
            likeCount = 7L,
        )

        val result = catalogService.getProductDetails(productId = 10L)

        result.productId shouldBe 10L
        result.brandName shouldBe "브랜드10"
        result.categoryId shouldBe 10L
        result.images.map { it.imageUrl } shouldBe
            listOf(
                "https://cdn.example.com/product-10-primary.jpg",
                "https://cdn.example.com/product-10-secondary.jpg",
            )
        result.options.map { it.productOptionId } shouldBe listOf(101L, 102L)
        result.options.map { it.additionalPrice } shouldBe listOf(0L, 1_000L)
        result.likeCount shouldBe 7L
        result.saleStatus shouldBe ProductSaleStatus.AVAILABLE
    }

    private fun product(
        id: Long,
        price: Long,
        categoryId: Long,
    ): Product {
        return Product(
            id = id,
            brandId = 1L,
            name = "상품$id",
            description = "상품 설명$id",
            basePrice = Money(price),
            categoryId = categoryId,
            images =
                mutableListOf(
                    ProductImage(
                        imageUrl = "https://cdn.example.com/product-$id-primary.jpg",
                        isPrimary = true,
                        sortOrder = 0,
                    ),
                    ProductImage(
                        imageUrl = "https://cdn.example.com/product-$id-secondary.jpg",
                        isPrimary = false,
                        sortOrder = 1,
                    ),
                ),
            options =
                mutableListOf(
                    ProductOption(
                        color = "BLACK",
                        size = "M",
                        sortOder = 0,
                        additionalPrice = Money(0),
                    ),
                    ProductOption(
                        color = "WHITE",
                        size = "L",
                        sortOder = 1,
                        additionalPrice = Money(1_000),
                    ),
                ),
        )
    }
}

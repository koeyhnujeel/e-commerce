package zoonza.commerce.catalog.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductDetailQueryResult
import zoonza.commerce.catalog.application.port.out.ProductImageQueryResult
import zoonza.commerce.catalog.application.port.out.ProductOptionQueryResult
import zoonza.commerce.catalog.application.port.out.ProductQueryRepository
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryResult
import zoonza.commerce.catalog.domain.category.CategoryRepository
import zoonza.commerce.catalog.domain.product.*
import zoonza.commerce.like.LikeApi
import zoonza.commerce.shared.Money
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

class DefaultCatalogServiceTest {
    private val productRepository = mockk<ProductRepository>()
    private val productQueryRepository = mockk<ProductQueryRepository>()
    private val categoryRepository = mockk<CategoryRepository>()
    private val likeApi = mockk<LikeApi>()
    private val catalogService =
        DefaultCatalogService(
            productRepository = productRepository,
            productQueryRepository = productQueryRepository,
            categoryRepository = categoryRepository,
            likeApi = likeApi,
        )

    @Test
    fun `상품 목록 조회는 정렬과 좋아요 정보를 함께 조합한다`() {
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
                    primaryImageUrl = "https://cdn.example.com/product-20-primary.jpg",
                    basePrice = 39_900,
                    likeCount = 5L,
                    saleStatus = ProductSaleStatus.AVAILABLE,
                ),
                ProductSummaryQueryResult(
                    productId = 10L,
                    name = "상품10",
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
        every { likeApi.findLikedProductIds(1L, listOf(20L, 10L)) } returns setOf(20L)

        val result = catalogService.getProductsByCategory(
            memberId = 1L,
            page = 0,
            size = 20,
            categoryId = 1L,
            sort = ProductListSort.PRICE_DESC,
        )

        pageQuery.captured shouldBe PageQuery(page = 0, size = 20)
        result.items.map { it.productId } shouldBe listOf(20L, 10L)
        result.items.map { it.likeCount } shouldBe listOf(5L, 2L)
        result.items.map { it.likedByMe } shouldBe listOf(true, false)
        result.items.map { it.saleStatus } shouldBe listOf(ProductSaleStatus.AVAILABLE, ProductSaleStatus.AVAILABLE)
    }

    @Test
    fun `비로그인 상품 목록 조회는 likedByMe를 false로 반환한다`() {
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
                    primaryImageUrl = product.images.first { it.isPrimary }.imageUrl,
                    basePrice = product.basePrice.amount,
                    likeCount = 3L,
                    saleStatus = ProductSaleStatus.AVAILABLE,
                ),
            ),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
        )

        val result = catalogService.getProductsByCategory(
            memberId = null,
            page = 0,
            size = 20,
            categoryId = 1L,
            sort = ProductListSort.LATEST,
        )

        result.items.single().likedByMe shouldBe false
        verify(exactly = 1) { categoryRepository.findAllDescendantIds(1L) }
        verify(exactly = 0) { likeApi.findLikedProductIds(any(), any()) }
    }

    @Test
    fun `상품 상세 조회는 이미지 옵션 좋아요 정보를 함께 반환한다`() {
        every { productQueryRepository.findProductDetailsById(10L) } returns ProductDetailQueryResult(
            productId = 10L,
            name = "상품10",
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
        every { likeApi.findLikedProductIds(1L, listOf(10L)) } returns setOf(10L)

        val result = catalogService.getProductDetails(productId = 10L, memberId = 1L)

        result.productId shouldBe 10L
        result.categoryId shouldBe 10L
        result.images.map { it.imageUrl } shouldBe
            listOf(
                "https://cdn.example.com/product-10-primary.jpg",
                "https://cdn.example.com/product-10-secondary.jpg",
            )
        result.options.map { it.productOptionId } shouldBe listOf(101L, 102L)
        result.options.map { it.additionalPrice } shouldBe listOf(0L, 1_000L)
        result.likeCount shouldBe 7L
        result.likedByMe shouldBe true
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

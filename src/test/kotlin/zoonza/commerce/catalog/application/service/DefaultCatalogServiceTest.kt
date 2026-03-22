package zoonza.commerce.catalog.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductImage
import zoonza.commerce.catalog.domain.ProductSaleStatus
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult
import zoonza.commerce.like.LikeApi
import zoonza.commerce.shared.Money

class DefaultCatalogServiceTest {
    private val productRepository = mockk<ProductRepository>()
    private val likeApi = mockk<LikeApi>()
    private val catalogService =
        DefaultCatalogService(
            productRepository = productRepository,
            likeApi = likeApi,
        )

    @Test
    fun `상품 목록 조회는 정렬과 좋아요 정보를 함께 조합한다`() {
        val pageQuery = slot<PageQuery>()
        val expensiveProduct = product(id = 20L, price = 39_900, categoryIds = listOf(1L))
        val cheapProduct = product(id = 10L, price = 19_900, categoryIds = listOf(1L))

        every {
            productRepository.findAll(
                categoryId = 1L,
                pageQuery = capture(pageQuery),
                sort = ProductListSort.PRICE_DESC,
            )
        } returns PageResult(
            items = listOf(expensiveProduct, cheapProduct),
            page = 0,
            size = 20,
            totalElements = 2,
            totalPages = 1,
        )
        every {
            productRepository.findPrimaryImagesByProductIds(listOf(20L, 10L))
        } returns listOf(expensiveProduct.primaryImage(), cheapProduct.primaryImage())
        every { likeApi.countProductLikes(listOf(20L, 10L)) } returns mapOf(20L to 5L, 10L to 2L)
        every { likeApi.findLikedProductIds(1L, listOf(20L, 10L)) } returns setOf(20L)

        val result = catalogService.getProducts(
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
        val product = product(id = 10L, price = 19_900, categoryIds = listOf(1L))

        every {
            productRepository.findAll(
                categoryId = null,
                pageQuery = PageQuery(page = 0, size = 20),
                sort = ProductListSort.LATEST,
            )
        } returns PageResult(
            items = listOf(product),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
        )
        every { productRepository.findPrimaryImagesByProductIds(listOf(10L)) } returns listOf(product.primaryImage())
        every { likeApi.countProductLikes(listOf(10L)) } returns mapOf(10L to 3L)

        val result = catalogService.getProducts(
            memberId = null,
            page = 0,
            size = 20,
            categoryId = null,
            sort = ProductListSort.LATEST,
        )

        result.items.single().likedByMe shouldBe false
        verify(exactly = 0) { likeApi.findLikedProductIds(any(), any()) }
    }

    @Test
    fun `상품 상세 조회는 이미지 옵션 좋아요 정보를 함께 반환한다`() {
        val product = product(id = 10L, price = 19_900, categoryIds = listOf(20L, 10L))

        every { productRepository.findById(10L) } returns product
        every { productRepository.findImagesByProductId(10L) } returns product.images
        every { productRepository.findOptionsByProductId(10L) } returns product.options
        every { likeApi.countProductLikes(10L) } returns 7L
        every { likeApi.findLikedProductIds(1L, listOf(10L)) } returns setOf(10L)

        val result = catalogService.getProduct(productId = 10L, memberId = 1L)

        result.productId shouldBe 10L
        result.categoryIds shouldBe listOf(10L, 20L)
        result.images.map { it.imageUrl } shouldBe
            listOf(
                "https://cdn.example.com/product-10-primary.jpg",
                "https://cdn.example.com/product-10-secondary.jpg",
            )
        result.options.map { it.productOptionId } shouldBe listOf(0L, 0L)
        result.likeCount shouldBe 7L
        result.likedByMe shouldBe true
        result.saleStatus shouldBe ProductSaleStatus.AVAILABLE
    }

    private fun product(
        id: Long,
        price: Long,
        categoryIds: List<Long>,
    ): Product {
        return Product.create(
            id = id,
            brandId = 1L,
            name = "상품$id",
            description = "상품 설명$id",
            basePrice = Money(price),
            categoryIds = categoryIds,
            images =
                listOf(
                    ProductImage.create(
                        imageUrl = "https://cdn.example.com/product-$id-primary.jpg",
                        isPrimary = true,
                        sortOrder = 0,
                    ),
                    ProductImage.create(
                        imageUrl = "https://cdn.example.com/product-$id-secondary.jpg",
                        isPrimary = false,
                        sortOrder = 1,
                    ),
                ),
            options =
                listOf(
                    zoonza.commerce.catalog.domain.ProductOption.create(
                        color = "BLACK",
                        size = "M",
                        stockId = id * 10,
                    ),
                    zoonza.commerce.catalog.domain.ProductOption.create(
                        color = "WHITE",
                        size = "L",
                        stockId = id * 10 + 1,
                    ),
                ),
        )
    }
}

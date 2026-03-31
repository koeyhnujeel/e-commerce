package zoonza.commerce.member.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.support.fixture.MemberFixture

class MemberTest {
    @Test
    fun `첫 배송지는 기본 배송지로 지정된다`() {
        val member = MemberFixture.create()

        member.addAddress(
            MemberAddress.create(
                label = "집",
                recipientName = "주문자",
                recipientPhoneNumber = "01012345678",
                zipCode = "12345",
                baseAddress = "서울시 강남구",
                detailAddress = "",
                isDefault = false,
            ),
        )

        member.addresses.single().isDefault shouldBe true
    }

    @Test
    fun `기본 배송지를 변경하면 기존 기본 배송지는 해제된다`() {
        val member = MemberFixture.create()
        member.addAddress(
            MemberAddress(
                id = 1L,
                label = "집",
                recipientName = "주문자",
                recipientPhoneNumber = "01012345678",
                zipCode = "12345",
                baseAddress = "서울시 강남구",
                detailAddress = "",
                isDefault = true,
            ),
        )
        member.addAddress(
            MemberAddress(
                id = 2L,
                label = "회사",
                recipientName = "주문자",
                recipientPhoneNumber = "01099999999",
                zipCode = "54321",
                baseAddress = "서울시 서초구",
                detailAddress = "101호",
                isDefault = false,
            ),
        )

        member.changeDefaultAddress(2L)

        member.findAddress(2L).isDefault shouldBe true
        member.findAddress(1L).isDefault shouldBe false
    }

    @Test
    fun `기본 배송지를 삭제하면 다음 배송지가 기본 배송지가 된다`() {
        val member = MemberFixture.create()
        member.addAddress(
            MemberAddress(
                id = 1L,
                label = "집",
                recipientName = "주문자",
                recipientPhoneNumber = "01012345678",
                zipCode = "12345",
                baseAddress = "서울시 강남구",
                detailAddress = "",
                isDefault = true,
            ),
        )
        member.addAddress(
            MemberAddress(
                id = 2L,
                label = "회사",
                recipientName = "주문자",
                recipientPhoneNumber = "01099999999",
                zipCode = "54321",
                baseAddress = "서울시 서초구",
                detailAddress = "",
                isDefault = false,
            ),
        )

        member.removeAddress(1L)

        member.addresses.single().id shouldBe 2L
        member.addresses.single().isDefault shouldBe true
    }
}

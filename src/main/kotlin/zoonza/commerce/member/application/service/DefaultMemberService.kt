package zoonza.commerce.member.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.member.*
import zoonza.commerce.member.application.dto.CreateMemberAddressCommand
import zoonza.commerce.member.application.dto.SignupCommand
import zoonza.commerce.member.application.dto.UpdateMemberAddressCommand
import zoonza.commerce.member.application.port.`in`.MemberService
import zoonza.commerce.member.application.port.out.MemberRepository
import zoonza.commerce.member.application.port.out.NicknameGenerator
import zoonza.commerce.member.domain.Member
import zoonza.commerce.member.domain.MemberAddress
import zoonza.commerce.member.domain.PasswordEncoder
import zoonza.commerce.shared.AuthErrorCode
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.VerificationApi
import java.time.LocalDateTime

@Service
class DefaultMemberService(
    private val memberRepository: MemberRepository,
    private val verificationApi: VerificationApi,
    private val passwordEncoder: PasswordEncoder,
    private val nicknameGenerator: NicknameGenerator,
) : MemberService, MemberApi {
    companion object {
        private const val MAX_NICKNAME_GENERATION_ATTEMPTS = 100
    }

    @Transactional
    override fun sendSignupEmailVerificationCode(email: String) {
        val emailVO = Email(email)

        if (memberRepository.existsByEmail(emailVO)) {
            throw BusinessException(MemberErrorCode.DUPLICATE_EMAIL)
        }

        verificationApi.createSignupEmailVerificationCode(emailVO)
    }

    @Transactional
    override fun verifySignupEmailCode(email: String, code: String) {
        verificationApi.verifySignupEmailVerificationCode(
            email = Email(email),
            code = code,
        )
    }

    @Transactional
    override fun signup(command: SignupCommand): Long {
        val emailVO = Email(command.email)

        if (memberRepository.existsByEmail(emailVO)) {
            throw BusinessException(MemberErrorCode.DUPLICATE_EMAIL)
        }

        if (memberRepository.existsByPhoneNumber(command.phoneNumber)) {
            throw BusinessException(MemberErrorCode.DUPLICATE_PHONE_NUMBER)
        }

        verificationApi.assertVerifiedSignupEmail(emailVO)

        val member = Member.create(
            email = emailVO,
            passwordHash = passwordEncoder.encode(command.password),
            name = command.name,
            nickname = generateUniqueNickname(),
            phoneNumber = command.phoneNumber,
            registeredAt = LocalDateTime.now(),
        )

        return memberRepository.save(member).id
    }

    @Transactional
    override fun authenticate(email: Email, password: String): AuthenticatedMember {
        val member = memberRepository.findByEmail(email)
            ?: throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)

        if (!member.verifyPassword(password, passwordEncoder)) {
            throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)
        }

        member.recordLogin(LocalDateTime.now())

        return AuthenticatedMember(member.id, member.email, member.role.name)
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): AuthenticatedMember? {
        return memberRepository.findById(id)
            ?.let { member -> AuthenticatedMember(member.id, member.email, member.role.name) }
    }

    @Transactional(readOnly = true)
    override fun findProfileById(id: Long): MemberProfile {
        val member = memberRepository.findById(id)
            ?: throw BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)

        return MemberProfile(member.id, member.nickname)
    }

    @Transactional(readOnly = true)
    override fun findProfilesByIds(ids: Set<Long>): Map<Long, MemberProfile> {
        if (ids.isEmpty()) {
            return emptyMap()
        }

        val profiles = memberRepository.findAllByIds(ids)
            .associate { member -> member.id to MemberProfile(member.id, member.nickname) }

        if (profiles.size != ids.size) {
            throw BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)
        }

        return profiles
    }

    @Transactional(readOnly = true)
    override fun getMyAddresses(memberId: Long): List<MemberAddressSnapshot> {
        return findMemberOrThrow(memberId)
            .addresses
            .map(::toAddressSnapshot)
    }

    @Transactional
    override fun addAddress(
        memberId: Long,
        command: CreateMemberAddressCommand,
    ): Long {
        val member = findMemberOrThrow(memberId)
        member.addAddress(
            MemberAddress.create(
                label = command.label,
                recipientName = command.recipientName,
                recipientPhoneNumber = command.recipientPhoneNumber,
                zipCode = command.zipCode,
                baseAddress = command.baseAddress,
                detailAddress = command.detailAddress,
                isDefault = command.isDefault,
            ),
        )

        return memberRepository.save(member)
            .addresses
            .maxBy { it.id }
            .id
    }

    @Transactional
    override fun updateAddress(
        memberId: Long,
        addressId: Long,
        command: UpdateMemberAddressCommand,
    ) {
        val member = findMemberOrThrow(memberId)
        ensureAddressExists(member, addressId)
        member.updateAddress(
            addressId = addressId,
            label = command.label,
            recipientName = command.recipientName,
            recipientPhoneNumber = command.recipientPhoneNumber,
            zipCode = command.zipCode,
            baseAddress = command.baseAddress,
            detailAddress = command.detailAddress,
            isDefault = command.isDefault,
        )
        memberRepository.save(member)
    }

    @Transactional
    override fun removeAddress(
        memberId: Long,
        addressId: Long,
    ) {
        val member = findMemberOrThrow(memberId)
        ensureAddressExists(member, addressId)
        member.removeAddress(addressId)
        memberRepository.save(member)
    }

    @Transactional
    override fun changeDefaultAddress(
        memberId: Long,
        addressId: Long,
    ) {
        val member = findMemberOrThrow(memberId)
        ensureAddressExists(member, addressId)
        member.changeDefaultAddress(addressId)
        memberRepository.save(member)
    }

    @Transactional(readOnly = true)
    override fun findShippingAddress(
        memberId: Long,
        addressId: Long,
    ): MemberAddressSnapshot {
        val member = findMemberOrThrow(memberId)
        return try {
            toAddressSnapshot(member.findAddress(addressId))
        } catch (_: IllegalArgumentException) {
            throw BusinessException(MemberErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }
    }

    @Transactional(readOnly = true)
    override fun findDefaultShippingAddress(memberId: Long): MemberAddressSnapshot {
        val member = findMemberOrThrow(memberId)
        return try {
            toAddressSnapshot(member.defaultAddress())
        } catch (_: IllegalArgumentException) {
            throw BusinessException(MemberErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }
    }

    private fun generateUniqueNickname(): String {
        repeat(MAX_NICKNAME_GENERATION_ATTEMPTS) {
            val nickname = nicknameGenerator.generate()

            if (!memberRepository.existsByNickname(nickname)) {
                return nickname
            }
        }

        throw IllegalStateException("중복되지 않는 닉네임을 생성하지 못했습니다.")
    }

    private fun findMemberOrThrow(memberId: Long): Member {
        return memberRepository.findById(memberId)
            ?: throw BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    private fun ensureAddressExists(
        member: Member,
        addressId: Long,
    ) {
        try {
            member.findAddress(addressId)
        } catch (_: IllegalArgumentException) {
            throw BusinessException(MemberErrorCode.MEMBER_ADDRESS_NOT_FOUND)
        }
    }

    private fun toAddressSnapshot(address: MemberAddress): MemberAddressSnapshot {
        return MemberAddressSnapshot(
            id = address.id,
            label = address.label,
            recipientName = address.recipientName,
            recipientPhoneNumber = address.recipientPhoneNumber,
            zipCode = address.zipCode,
            baseAddress = address.baseAddress,
            detailAddress = address.detailAddress,
            isDefault = address.isDefault,
        )
    }
}

package zoonza.commerce.payment.adapter.`in`

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.common.ApiResponse
import zoonza.commerce.payment.adapter.`in`.request.CreatePaymentRequest
import zoonza.commerce.payment.adapter.`in`.response.CreatePaymentResponse
import zoonza.commerce.payment.adapter.`in`.response.PaymentDetailResponse
import zoonza.commerce.payment.adapter.`in`.response.toResponse
import zoonza.commerce.payment.application.dto.CreatePaymentCommand
import zoonza.commerce.payment.application.port.`in`.PaymentService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.security.CurrentMemberInfo

@RestController
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping("/api/orders/{orderId}/payments")
    fun createPayment(
        @CurrentMember currentMember: CurrentMemberInfo,
        @PathVariable orderId: Long,
        @Valid @RequestBody request: CreatePaymentRequest,
    ): ApiResponse<CreatePaymentResponse> {
        val createdPayment = paymentService.createPayment(
            memberId = currentMember.memberId,
            orderId = orderId,
            command =
                CreatePaymentCommand(
                    amount = request.amount,
                    paymentMethod = request.paymentMethod,
                ),
        )

        return ApiResponse.success(
            CreatePaymentResponse(
                paymentId = createdPayment.paymentId,
                orderId = createdPayment.orderId,
                orderNumber = createdPayment.orderNumber,
                status = createdPayment.status,
                amount = createdPayment.amount,
                checkout = createdPayment.checkout.toResponse(),
                createdAt = createdPayment.createdAt,
            ),
        )
    }

    @GetMapping("/api/payments/{paymentId}")
    fun getPayment(
        @CurrentMember currentMember: CurrentMemberInfo,
        @PathVariable paymentId: Long,
    ): ApiResponse<PaymentDetailResponse> {
        val payment = paymentService.getPayment(
            memberId = currentMember.memberId,
            paymentId = paymentId,
        )

        return ApiResponse.success(
            PaymentDetailResponse(
                paymentId = payment.paymentId,
                orderId = payment.orderId,
                orderNumber = payment.orderNumber,
                status = payment.status,
                paymentMethod = payment.paymentMethod,
                amount = payment.amount,
                paymentKey = payment.paymentKey,
                providerReference = payment.providerReference,
                failureReason = payment.failureReason,
                createdAt = payment.createdAt,
                approvedAt = payment.approvedAt,
                canceledAt = payment.canceledAt,
            ),
        )
    }
}

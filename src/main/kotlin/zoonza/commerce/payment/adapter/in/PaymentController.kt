package zoonza.commerce.payment.adapter.`in`

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.common.ApiResponse
import zoonza.commerce.payment.adapter.`in`.request.CancelPaymentRequest
import zoonza.commerce.payment.adapter.`in`.request.ConfirmPaymentRequest
import zoonza.commerce.payment.adapter.`in`.request.CreatePaymentRequest
import zoonza.commerce.payment.adapter.`in`.response.CreatePaymentResponse
import zoonza.commerce.payment.adapter.`in`.response.PaymentDetailResponse
import zoonza.commerce.payment.adapter.`in`.response.toResponse
import zoonza.commerce.payment.application.dto.CancelPaymentCommand
import zoonza.commerce.payment.application.dto.ConfirmPaymentCommand
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

    @PostMapping("/api/payments/{paymentId}/confirm")
    fun confirmPayment(
        @CurrentMember currentMember: CurrentMemberInfo,
        @PathVariable paymentId: Long,
        @Valid @RequestBody request: ConfirmPaymentRequest,
    ): ApiResponse<PaymentDetailResponse> {
        val payment = paymentService.confirmPayment(
            memberId = currentMember.memberId,
            paymentId = paymentId,
            command =
                ConfirmPaymentCommand(
                    paymentKey = request.paymentKey,
                    orderId = request.orderId,
                    amount = request.amount,
                ),
        )

        return ApiResponse.success(payment.toResponse())
    }

    @PostMapping("/api/payments/{paymentId}/cancel")
    fun cancelPayment(
        @CurrentMember currentMember: CurrentMemberInfo,
        @PathVariable paymentId: Long,
        @Valid @RequestBody request: CancelPaymentRequest,
    ): ApiResponse<PaymentDetailResponse> {
        val payment = paymentService.cancelPayment(
            memberId = currentMember.memberId,
            paymentId = paymentId,
            command = CancelPaymentCommand(reason = request.reason),
        )

        return ApiResponse.success(payment.toResponse())
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

        return ApiResponse.success(payment.toResponse())
    }
}

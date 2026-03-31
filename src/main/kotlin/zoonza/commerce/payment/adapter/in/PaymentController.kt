package zoonza.commerce.payment.adapter.`in`

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.payment.adapter.`in`.request.PaymentWebhookRequest
import zoonza.commerce.payment.adapter.`in`.response.PreparePaymentResponse
import zoonza.commerce.payment.application.port.`in`.PaymentService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.web.ApiResponse

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping("/orders/{orderId}/prepare")
    fun prepare(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderId: Long,
    ): ApiResponse<PreparePaymentResponse> {
        return ApiResponse.success(
            PreparePaymentResponse.from(
                paymentService.prepare(
                    memberId = currentMember.memberId,
                    orderId = orderId,
                ),
            ),
        )
    }

    @GetMapping("/toss/success")
    fun successCallback(
        @RequestParam("token") callbackToken: String,
        @RequestParam("paymentKey") paymentKey: String,
        @RequestParam("orderId") providerOrderId: String,
        @RequestParam("amount") amount: Long,
    ): ResponseEntity<Void> {
        val result =
            paymentService.handleSuccessCallback(
                callbackToken = callbackToken,
                providerOrderId = providerOrderId,
                paymentKey = paymentKey,
                amount = amount,
            )

        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .header(HttpHeaders.LOCATION, result.redirectUrl)
            .build()
    }

    @GetMapping("/toss/fail")
    fun failCallback(
        @RequestParam("token") callbackToken: String,
        @RequestParam("code") code: String,
        @RequestParam("message") message: String,
    ): ResponseEntity<Void> {
        val result =
            paymentService.handleFailCallback(
                callbackToken = callbackToken,
                code = code,
                message = message,
            )

        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .header(HttpHeaders.LOCATION, result.redirectUrl)
            .build()
    }

    @PostMapping("/toss/webhooks")
    fun webhook(
        @RequestBody request: PaymentWebhookRequest,
    ): ApiResponse<Nothing> {
        paymentService.handleWebhook(
            eventType = request.eventType,
            paymentKey = request.data.paymentKey,
            providerOrderId = request.data.orderId,
        )
        return ApiResponse.success()
    }
}

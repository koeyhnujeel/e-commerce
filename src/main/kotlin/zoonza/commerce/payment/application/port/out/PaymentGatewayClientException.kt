package zoonza.commerce.payment.application.port.out

class PaymentGatewayClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

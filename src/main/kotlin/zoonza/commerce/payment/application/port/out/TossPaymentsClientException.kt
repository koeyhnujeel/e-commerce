package zoonza.commerce.payment.application.port.out

class TossPaymentsClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

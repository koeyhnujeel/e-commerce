package zoonza.commerce.payment.adapter.out.toss

class TossPaymentsClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

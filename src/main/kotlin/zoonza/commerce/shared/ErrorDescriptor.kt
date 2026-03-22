package zoonza.commerce.shared

interface ErrorDescriptor {
    val code: String
    val message: String
    val status: ErrorStatus
}

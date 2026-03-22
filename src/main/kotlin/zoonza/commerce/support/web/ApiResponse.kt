package zoonza.commerce.support.web

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(success = true, data = data)

        fun <Nothing> success(): ApiResponse<Nothing> =
            ApiResponse(success = true)

        fun <Nothing> error(errorResponse: ErrorResponse): ApiResponse<Nothing> =
            ApiResponse(success = false, error = errorResponse)
    }
}

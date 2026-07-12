package com.manruhomerun.yadanbeopseok.network.common.error

import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.network.common.dto.ApiErrorResponseDto
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * 모든 Retrofit API 호출에서 발생하는 예외를
 * 앱 내부에서 사용하는 공통 예외로 변환합니다.
 */
class ApiCallExecutor @Inject constructor(
    private val json: Json,
) {
    /**
     * Retrofit API를 호출하고 발생한 예외를 앱 공통 예외로 변환합니다.
     *
     * 코루틴의 CancellationException은 잡지 않으므로 그대로 전달됩니다.
     */
    suspend fun <T> execute(
        apiCall: suspend () -> T,
    ): T {
        return try {
            apiCall()
        } catch (exception: InterruptedIOException) {
            throw NetworkTimeoutException(cause = exception)
        } catch (exception: HttpException) {
            throw exception.toApiException()
        } catch (exception: SerializationException) {
            throw InvalidResponseException(cause = exception)
        } catch (exception: IOException) {
            throw NetworkConnectionException(cause = exception)
        }
    }

    /**
     * 백엔드의 HTTP 오류 응답을 ApiException으로 변환합니다.
     *
     * 에러 body를 읽거나 변환할 수 없으면 HTTP 상태 코드만 보존합니다.
     */
    private fun HttpException.toApiException(): ApiException {
        val errorResponse = parseErrorResponse()

        return ApiException(
            statusCode = code(),
            errorCode = errorResponse?.code,
            message = errorResponse?.message ?: "Request failed with HTTP ${code()}.",
            path = errorResponse?.path,
            cause = this,
        )
    }

    /**
     * HTTP error body를 백엔드 공통 에러 응답 형식으로 변환합니다.
     */
    private fun HttpException.parseErrorResponse(): ApiErrorResponseDto? {
        val errorBody = try {
            response()?.errorBody()?.string()
        } catch (_: IOException) {
            null
        }

        if (errorBody.isNullOrBlank()) {
            return null
        }

        return try {
            json.decodeFromString<ApiErrorResponseDto>(errorBody)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

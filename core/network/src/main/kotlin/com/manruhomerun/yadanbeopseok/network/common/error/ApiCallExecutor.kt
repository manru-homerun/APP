package com.manruhomerun.yadanbeopseok.network.common.error

import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.network.auth.token.AuthSessionProvider
import com.manruhomerun.yadanbeopseok.network.common.dto.ApiErrorResponseDto
import java.io.IOException
import java.io.InterruptedIOException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
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
    private val authSessionProvider: AuthSessionProvider,
) {
    /**
     * 현재 인증 세션에 속하는 Retrofit API를 호출합니다.
     *
     * 로그인 이외의 API에서 최종 401 응답을 받으면
     * 로컬 인증 세션을 삭제하고 세션 만료 예외로 변환합니다.
     */
    suspend fun <T> execute(apiCall: suspend () -> T): T {
        return executeInternal(
            expiresSessionOnUnauthorized = true,
            apiCall = apiCall,
        )
    }

    /**
     * 로그인 API를 호출합니다.
     *
     * 로그인 요청의 401은 기존 세션 만료가 아닌 로그인 실패이므로
     * 일반 ApiException으로 변환합니다.
     */
    suspend fun <T> executeLogin(apiCall: suspend () -> T): T {
        return executeInternal(
            expiresSessionOnUnauthorized = false,
            apiCall = apiCall,
        )
    }

    /**
     * Retrofit API를 호출하고 발생한 예외를 앱 공통 예외로 변환합니다.
     *
     * 코루틴의 CancellationException은 잡지 않으므로 그대로 전달됩니다.
     */
    private suspend fun <T> executeInternal(
        expiresSessionOnUnauthorized: Boolean,
        apiCall: suspend () -> T,
    ): T {
        return try {
            apiCall()
        } catch (exception: InterruptedIOException) {
            throw NetworkTimeoutException(cause = exception)
        } catch (exception: HttpException) {
            val apiException = exception.toApiException()
            if (expiresSessionOnUnauthorized && apiException.statusCode == HTTP_UNAUTHORIZED) {
                authSessionProvider.clearSession()
                throw SessionExpiredException(cause = apiException)
            }
            throw apiException
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

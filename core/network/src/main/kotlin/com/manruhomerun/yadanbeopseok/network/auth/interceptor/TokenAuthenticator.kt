package com.manruhomerun.yadanbeopseok.network.auth.interceptor

import com.manruhomerun.yadanbeopseok.common.AppException
import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.network.auth.token.AuthSessionProvider
import com.manruhomerun.yadanbeopseok.network.auth.token.TokenRefreshHandler
import dagger.Lazy
import java.io.IOException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * 인증된 요청에서 401 응답을 받으면 서비스 토큰을 재발급하고
 * 실패했던 요청을 새로운 Access Token으로 다시 실행합니다.
 */
@Singleton
internal class TokenAuthenticator @Inject constructor(
    private val tokenRefreshHandler: Lazy<TokenRefreshHandler>,
    private val authSessionProvider: AuthSessionProvider,
) : Authenticator {
    /**
     * 동시 401의 재발급을 직렬화하고, 성공한 요청이 저장한 토큰을 재사용하도록 합니다.
     */
    private val refreshLock = Any()

    /**
     * 401 응답을 처리하고 새로운 인증 헤더가 준비되면 재시도할 요청을 반환합니다.
     *
     * 세션이 만료되었거나 이미 재시도한 요청이면 null을 반환합니다.
     * 일시적인 재발급 실패는 원래 401과 구분할 수 있도록 원인을 보존해 전달합니다.
     */
    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        val failedRequest = response.request

        if (
            failedRequest.isPublicAuthRequest() ||
            response.unauthorizedResponseCount() >= MAX_UNAUTHORIZED_RESPONSE_COUNT
        ) {
            return null
        }

        val failedAuthorizationHeader =
            failedRequest.header(AUTHORIZATION_HEADER_NAME)
                ?: return null

        return try {
            synchronized(refreshLock) {
                val currentAuthorizationHeader = getCurrentAuthorizationHeader()

                /*
                 * 다른 요청이 먼저 재발급을 완료했다면 다시 재발급하지 않고
                 * 이미 저장된 새로운 Access Token으로 요청만 재시도합니다.
                 */
                if (
                    currentAuthorizationHeader != null &&
                    currentAuthorizationHeader != failedAuthorizationHeader
                ) {
                    return@synchronized failedRequest.withAuthorizationHeader(
                        currentAuthorizationHeader,
                    )
                }

                val refreshedAuthorizationHeader = refreshAuthorizationHeader()
                    ?: return@synchronized null

                if (refreshedAuthorizationHeader == failedAuthorizationHeader) {
                    return@synchronized null
                }

                failedRequest.withAuthorizationHeader(refreshedAuthorizationHeader)
            }
        } catch (_: SessionExpiredException) {
            null
        } catch (exception: CancellationException) {
            throw TokenRefreshIOException(exception)
        } catch (exception: AppException) {
            throw TokenRefreshIOException(exception)
        }
    }

    /**
     * 현재 로컬에 저장된 인증 헤더를 조회합니다.
     */
    private fun getCurrentAuthorizationHeader(): String? =
        runBlocking {
            authSessionProvider.getAuthorizationHeader()
        }

    /**
     * Data 계층에 토큰 재발급을 요청한 후 새 인증 헤더를 조회합니다.
     *
     * 실패 원인은 authenticate에서 인증 만료와 일시적인 오류로 구분합니다.
     */
    private fun refreshAuthorizationHeader(): String? =
        runBlocking {
            tokenRefreshHandler.get().refreshToken()
            authSessionProvider.getAuthorizationHeader()
        }
}

/**
 * 재발급 오류나 취소를 OkHttp의 IOException 경계로 전달합니다.
 * ApiCallExecutor에서 원본 예외를 복원하므로 인증 만료나 연결 오류로 오인하지 않습니다.
 */
internal class TokenRefreshIOException(val originalException: RuntimeException) :
    IOException("Token refresh failed.", originalException)

/**
 * 요청의 기존 Authorization 헤더를 새로운 값으로 교체합니다.
 */
private fun Request.withAuthorizationHeader(
    authorizationHeader: String,
): Request =
    newBuilder()
        .header(AUTHORIZATION_HEADER_NAME, authorizationHeader)
        .build()

/**
 * 현재 응답까지 발생한 401 응답 수를 계산합니다.
 *
 * 재발급 후 다시 401이 발생했을 때 인증 요청이 무한 반복되는 것을 방지합니다.
 */
private fun Response.unauthorizedResponseCount(): Int {
    var count = 0
    var currentResponse: Response? = this

    while (currentResponse != null) {
        if (currentResponse.code == HTTP_UNAUTHORIZED) {
            count += 1
        }

        currentResponse = currentResponse.priorResponse
    }

    return count
}

private const val MAX_UNAUTHORIZED_RESPONSE_COUNT = 2

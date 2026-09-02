package com.manruhomerun.yadanbeopseok.network.auth.interceptor

import com.manruhomerun.yadanbeopseok.common.error.AppException
import com.manruhomerun.yadanbeopseok.network.auth.token.AuthSessionProvider
import com.manruhomerun.yadanbeopseok.network.auth.token.TokenRefreshHandler
import dagger.Lazy
import java.io.IOException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Singleton
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
     * 여러 요청이 동시에 401을 받더라도 재발급 요청이 한 번만 실행되도록 사용합니다.
     */
    private val refreshLock = Any()

    /**
     * 401 응답을 처리하고 새로운 인증 헤더가 준비되면 재시도할 요청을 반환합니다.
     *
     * 재발급할 수 없거나 이미 재시도한 요청이면 null을 반환하여
     * OkHttp가 더 이상 인증 재시도를 수행하지 않게 합니다.
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

        return synchronized(refreshLock) {
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

            val refreshedAuthorizationHeader =
                refreshAuthorizationHeader()
                    ?: return@synchronized null

            if (refreshedAuthorizationHeader == failedAuthorizationHeader) {
                return@synchronized null
            }

            failedRequest.withAuthorizationHeader(
                refreshedAuthorizationHeader,
            )
        }
    }

    /**
     * 현재 로컬에 저장된 인증 헤더를 조회합니다.
     */
    private fun getCurrentAuthorizationHeader(): String? =
        try {
            runBlocking {
                authSessionProvider.getAuthorizationHeader()
            }
        } catch (_: IOException) {
            null
        }

    /**
     * Data 계층에 토큰 재발급을 요청한 후 새 인증 헤더를 조회합니다.
     *
     * 재발급 실패는 원래 401 응답이 호출자에게 전달되도록 null로 처리합니다.
     */
    private fun refreshAuthorizationHeader(): String? =
        try {
            runBlocking {
                tokenRefreshHandler.get().refreshToken()
                authSessionProvider.getAuthorizationHeader()
            }
        } catch (_: AppException) {
            null
        } catch (_: IOException) {
            null
        }
}

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

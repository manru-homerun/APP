package com.manruhomerun.yadanbeopseok.network.auth.interceptor

import com.manruhomerun.yadanbeopseok.network.auth.token.AuthorizationHeaderProvider
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 인증이 필요한 네트워크 요청에 Authorization 헤더를 추가합니다.
 *
 * 토큰의 저장 방식은 알지 않고 [AuthorizationHeaderProvider]를 통해
 * 현재 사용할 인증 헤더만 전달받습니다.
 */
class AuthInterceptor @Inject constructor(
    private val authorizationHeaderProvider: AuthorizationHeaderProvider,
) : Interceptor {
    /**
     * 로그인과 토큰 재발급 요청을 제외한 요청에 인증 헤더를 추가합니다.
     *
     * 저장된 인증 정보가 없거나 요청에 이미 인증 헤더가 있다면
     * 원본 요청을 변경하지 않고 그대로 진행합니다.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (
            request.isPublicAuthRequest() ||
            request.header(AUTHORIZATION_HEADER_NAME) != null
        ) {
            return chain.proceed(request)
        }

        val authorizationHeader =
            runBlocking {
                authorizationHeaderProvider.getAuthorizationHeader()
            } ?: return chain.proceed(request)

        val authenticatedRequest =
            request
                .newBuilder()
                .header(AUTHORIZATION_HEADER_NAME, authorizationHeader)
                .build()

        return chain.proceed(authenticatedRequest)
    }
}

/**
 * Access Token을 사용하지 않아야 하는 공개 인증 요청인지 확인합니다.
 *
 * 이 함수는 이후 [TokenAuthenticator]에서도 재사용할 수 있도록
 * internal로 선언합니다.
 */
internal fun Request.isPublicAuthRequest(): Boolean =
    PUBLIC_AUTH_PATH_SUFFIXES.any { pathSuffix ->
        url.encodedPath.endsWith(pathSuffix)
    }

internal const val AUTHORIZATION_HEADER_NAME = "Authorization"

private val PUBLIC_AUTH_PATH_SUFFIXES =
    setOf(
        "/auth/login",
        "/auth/refresh",
    )

package com.manruhomerun.yadanbeopseok.data.auth

import com.manruhomerun.yadanbeopseok.datastore.AuthTokenDataSource
import com.manruhomerun.yadanbeopseok.network.auth.token.AuthorizationHeaderProvider
import javax.inject.Inject

/**
 * DataStore에 저장된 야단법석 서비스 토큰으로
 * 네트워크 요청에 사용할 Authorization 헤더를 생성합니다.
 */
internal class AuthorizationHeaderProviderImpl @Inject constructor(
    private val authTokenDataSource: AuthTokenDataSource,
) : AuthorizationHeaderProvider {
    /**
     * 저장된 token type과 access token을 조합해 인증 헤더를 반환합니다.
     *
     * 인증 정보가 없거나 일부 값이 누락된 경우 null을 반환합니다.
     * Access Token 만료 처리는 이후 TokenAuthenticator가 담당합니다.
     */
    override suspend fun getAuthorizationHeader(): String? {
        val authTokens = authTokenDataSource.getAuthTokens() ?: return null

        return "${authTokens.tokenType} ${authTokens.accessToken}"
    }
}

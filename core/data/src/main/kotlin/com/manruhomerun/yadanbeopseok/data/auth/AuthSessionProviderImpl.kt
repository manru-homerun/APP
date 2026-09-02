package com.manruhomerun.yadanbeopseok.data.auth

import com.manruhomerun.yadanbeopseok.datastore.AuthTokenDataSource
import com.manruhomerun.yadanbeopseok.network.auth.token.AuthSessionProvider
import javax.inject.Inject

/**
 * DataStore에 저장된 야단법석 인증 세션을
 * Network 계층에 필요한 형태로 제공합니다.
 */
internal class AuthSessionProviderImpl @Inject constructor(
    private val authTokenDataSource: AuthTokenDataSource,
) : AuthSessionProvider {
    /**
     * 저장된 token type과 access token을 조합해 인증 헤더를 반환합니다.
     *
     * 인증 정보가 없거나 일부 값이 누락된 경우 null을 반환합니다.
     * Access Token 만료 처리는 TokenAuthenticator가 담당합니다.
     */
    override suspend fun getAuthorizationHeader(): String? {
        val authTokens = authTokenDataSource.getAuthTokens() ?: return null

        return "${authTokens.tokenType} ${authTokens.accessToken}"
    }

    /**
     * 더 이상 사용할 수 없는 로컬 인증 세션을 삭제합니다.
     */
    override suspend fun clearSession() {
        authTokenDataSource.clearAuthTokens()
    }
}

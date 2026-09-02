package com.manruhomerun.yadanbeopseok.network.auth.token

/**
 * Network 계층에서 사용할 현재 인증 세션을 제공합니다.
 *
 * Network 모듈은 토큰의 저장 방식을 알지 않고,
 * 이 인터페이스를 통해 인증 헤더를 조회하거나 만료된 세션을 삭제합니다.
 */
interface AuthSessionProvider {
    /**
     * 현재 저장된 서비스 토큰으로 완성된 Authorization 헤더를 반환합니다.
     *
     * 로그인하지 않았거나 유효한 인증 정보가 없으면 null을 반환합니다.
     *
     * 반환 예시: "Bearer access-token"
     */
    suspend fun getAuthorizationHeader(): String?

    /**
     * 최종 401 응답으로 더 이상 사용할 수 없는 인증 세션을 삭제합니다.
     */
    suspend fun clearSession()
}

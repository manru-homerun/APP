package com.manruhomerun.yadanbeopseok.network.auth.token

/**
 * 인증이 필요한 네트워크 요청에 사용할 Authorization 헤더를 제공합니다.
 *
 * Network 모듈은 토큰의 저장 방식을 알지 않고,
 * 이 인터페이스를 통해 현재 인증 헤더만 전달받습니다.
 */
interface AuthorizationHeaderProvider {
    /**
     * 현재 저장된 서비스 토큰으로 완성된 Authorization 헤더를 반환합니다.
     *
     * 로그인하지 않았거나 유효한 인증 정보가 없으면 null을 반환합니다.
     *
     * 반환 예시: "Bearer access-token"
     */
    suspend fun getAuthorizationHeader(): String?
}

package com.manruhomerun.yadanbeopseok.network.auth.token

/**
 * 만료된 Access Token을 재발급하기 위한 동작을 정의합니다.
 *
 * Network 모듈은 실제 Repository 구현을 알지 않고,
 * 이 인터페이스를 통해 Data 계층에 토큰 재발급을 요청합니다.
 */
interface TokenRefreshHandler {
    /**
     * 저장된 Refresh Token으로 서비스 토큰을 재발급합니다.
     *
     * 성공하면 새 Access Token과 Refresh Token이 로컬 저장소에 저장됩니다.
     * 재발급할 수 없는 경우 구현체에서 발생한 예외를 호출자에게 전달합니다.
     */
    suspend fun refreshToken()
}

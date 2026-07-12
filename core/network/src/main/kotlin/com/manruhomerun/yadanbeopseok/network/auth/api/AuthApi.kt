package com.manruhomerun.yadanbeopseok.network.auth.api

import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginResponseDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshResponseDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.WithdrawalResponseDto
import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 야단법석 백엔드의 인증 API를 정의합니다.
 */
interface AuthApi {
    /**
     * 카카오 액세스 토큰으로 야단법석 서비스에 로그인합니다.
     *
     * 신규 회원이면 백엔드에서 회원을 생성하고,
     * 야단법석 access token과 refresh token을 발급합니다.
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
        @Query("provider") provider: String = "KAKAO",
    ): ApiResponseDto<LoginResponseDto>

    /**
     * 야단법석 refresh token으로 서비스 토큰을 재발급합니다.
     *
     * 재발급된 access token과 refresh token은
     * 기존 토큰을 대체하여 함께 저장해야 합니다.
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequestDto,
    ): ApiResponseDto<TokenRefreshResponseDto>

    /**
     * 현재 로그인한 사용자의 회원 탈퇴를 요청합니다.
     *
     * Authorization 헤더는 추후 AuthInterceptor에서 추가합니다.
     */
    @DELETE("auth/withdrawal")
    suspend fun withdraw(): ApiResponseDto<WithdrawalResponseDto>
}

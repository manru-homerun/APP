package com.manruhomerun.yadanbeopseok.network.user.api

import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.NicknameAvailabilityResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.OnboardingRequestDto
import com.manruhomerun.yadanbeopseok.network.user.dto.OnboardingResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 야단법석 백엔드의 사용자 관련 API를 정의합니다.
 */
interface UserApi {
    /**
     * 로그인한 사용자가 입력한 닉네임의 중복 여부를 확인합니다.
     *
     * 야단법석 Access Token은 AuthInterceptor가
     * Authorization 헤더에 자동으로 추가합니다.
     *
     * @param nickname 앞뒤 공백을 제거한 확인 대상 닉네임
     * @return 닉네임 사용 가능 여부
     */
    @GET("users/me/nickname/check")
    suspend fun checkNicknameAvailability(
        @Query("nickname") nickname: String,
    ): NicknameAvailabilityResponseDto

    /**
     * 신규 사용자의 약관 동의, 기본 정보와 여행 취향을 저장하고
     * 온보딩을 완료합니다.
     *
     * 야단법석 Access Token은 AuthInterceptor가
     * Authorization 헤더에 자동으로 추가합니다.
     *
     * @param request 온보딩에서 입력한 사용자 정보
     * @return 온보딩을 완료한 사용자와 완료 상태
     */
    @POST("users/onboarding")
    suspend fun saveOnboarding(
        @Body request: OnboardingRequestDto,
    ): ApiResponseDto<OnboardingResponseDto>
}

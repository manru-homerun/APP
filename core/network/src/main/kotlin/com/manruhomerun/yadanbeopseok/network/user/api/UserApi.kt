package com.manruhomerun.yadanbeopseok.network.user.api

import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.NicknameAvailabilityResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.OnboardingRequestDto
import com.manruhomerun.yadanbeopseok.network.user.dto.OnboardingResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.TravelPreferenceResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.TravelPreferenceUpdateRequestDto
import com.manruhomerun.yadanbeopseok.network.user.dto.UserProfileResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.UserProfileUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * 야단법석 백엔드의 사용자 관련 API를 정의합니다.
 */
interface UserApi {
    /**
     * 현재 로그인한 사용자의 프로필을 조회합니다.
     */
    @GET("users/me/profile")
    suspend fun getMyProfile(): UserProfileResponseDto

    /**
     * 현재 로그인한 사용자의 프로필을 수정합니다.
     *
     * 성공 응답 Body는 사용하지 않습니다.
     */
    @PUT("users/me/profile")
    suspend fun updateMyProfile(@Body request: UserProfileUpdateRequestDto)

    /**
     * 현재 로그인한 사용자의 여행 취향을 조회합니다.
     */
    @GET("users/me/preference")
    suspend fun getMyTravelPreference(): TravelPreferenceResponseDto

    /**
     * 현재 로그인한 사용자의 여행 취향을 수정합니다.
     *
     * 성공 응답 Body는 사용하지 않습니다.
     */
    @PUT("users/me/preference")
    suspend fun updateMyTravelPreference(@Body request: TravelPreferenceUpdateRequestDto)

    /**
     * 로그인한 사용자가 입력한 닉네임의 중복 여부를 확인합니다.
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
     */
    @POST("users/onboarding")
    suspend fun saveOnboarding(
        @Body request: OnboardingRequestDto,
    ): ApiResponseDto<OnboardingResponseDto>
}

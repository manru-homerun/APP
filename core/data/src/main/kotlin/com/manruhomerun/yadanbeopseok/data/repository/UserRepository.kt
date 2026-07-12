package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import com.manruhomerun.yadanbeopseok.model.UserProfile

interface UserRepository {
    /**
     * 현재 로그인한 사용자의 프로필을 조회합니다.
     */
    suspend fun getMyProfile(): UserProfile

    /**
     * 현재 로그인한 사용자의 기본 프로필을 수정합니다.
     */
    suspend fun updateMyProfile(
        nickname: String?,
        profileImageUrl: String?,
        favoriteTeam: KboTeam?,
    ): UserProfile

    /**
     * 현재 로그인한 사용자의 여행 취향 정보를 조회합니다.
     */
    suspend fun getMyTravelPreference(): TravelPreference

    /**
     * 현재 로그인한 사용자의 여행 취향 정보를 수정합니다.
     */
    suspend fun updateMyTravelPreference(
        preference: TravelPreference,
    ): TravelPreference

    /**
     * 회원가입 직후 온보딩에서 입력한 닉네임, 응원 구단, 여행 취향을 저장합니다.
     */
    suspend fun saveOnboarding(
        nickname: String,
        favoriteTeam: KboTeam,
        travelPreference: TravelPreference,
    ): UserProfile
}

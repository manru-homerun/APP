package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import com.manruhomerun.yadanbeopseok.model.UserProfile

/**
 * 로그인한 사용자의 프로필과 여행 취향을 관리하는 Repository 계약입니다.
 */
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
}

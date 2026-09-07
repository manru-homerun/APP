package com.manruhomerun.yadanbeopseok.network.user.dto

import kotlinx.serialization.Serializable

/**
 * 현재 로그인한 사용자의 프로필 응답입니다.
 */
@Serializable
data class UserProfileResponseDto(
    val userId: String,
    val profileImageUrl: String? = null,
    val nickname: String,
    val favoriteTeam: FavoriteTeamResponseDto,
    val birthday: String,
    val gender: String,
)

/**
 * 사용자 프로필 응답에 포함된 응원 구단 정보입니다.
 */
@Serializable
data class FavoriteTeamResponseDto(
    val teamId: Long,
    val teamName: String,
    val logoImage: String,
)

/**
 * 현재 로그인한 사용자의 여행 취향 응답입니다.
 */
@Serializable
data class TravelPreferenceResponseDto(
    val travelStyleValue: Int,
    val residenceRegion: ProfileRegionResponseDto,
    val preferredRegions: List<ProfileRegionResponseDto>,
)

/**
 * 여행 취향 응답에 포함된 시도 지역 정보입니다.
 */
@Serializable
data class ProfileRegionResponseDto(
    val regionCode: String,
    val regionName: String,
)

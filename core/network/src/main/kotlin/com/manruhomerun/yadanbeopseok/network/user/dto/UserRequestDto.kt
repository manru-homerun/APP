package com.manruhomerun.yadanbeopseok.network.user.dto

import kotlinx.serialization.Serializable

/**
 * 현재 로그인한 사용자의 프로필 수정 요청입니다.
 *
 * @property profileImageUrl 프로필 이미지 URL
 * @property nickname 사용자 닉네임
 * @property favoriteTeamId 응원 구단 ID
 * @property birthday 생년월일(yyyy-MM-dd)
 * @property gender 성별 코드(MALE 또는 FEMALE)
 */
@Serializable
data class UserProfileUpdateRequestDto(
    val profileImageUrl: String?,
    val nickname: String,
    val favoriteTeamId: Long,
    val birthday: String,
    val gender: String,
)

/**
 * 현재 로그인한 사용자의 여행 취향 수정 요청입니다.
 *
 * API 명세에 따라 지역 코드는 사용하지 않고 지역 이름을 전달합니다.
 *
 * @property residenceRegion 거주 지역 이름
 * @property travelStyleValue 자연·도시 여행 성향 값
 * @property preferredRegions 선호 여행 지역 이름 목록
 */
@Serializable
data class TravelPreferenceUpdateRequestDto(
    val residenceRegion: String,
    val travelStyleValue: Int,
    val preferredRegions: List<String>,
)

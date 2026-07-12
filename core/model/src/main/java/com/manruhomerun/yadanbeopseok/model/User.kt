package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 사용자 프로필 모델입니다.
 *
 * ERD의 user 테이블을 참고하지만,
 * 앱 화면과 온보딩 흐름에서 필요한 사용자 정보를 중심으로 구성합니다.
 */
data class UserProfile(
    val id: String,
    val provider: LoginProvider,
    val providerUserId: String,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
    val favoriteTeam: KboTeam? = null,
    val gender: Gender? = null,
    val ageRange: String? = null,
    val onboardingCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

enum class LoginProvider {
    KAKAO,
    UNKNOWN,
}

enum class Gender {
    MALE,
    FEMALE,
    UNKNOWN,
}

/**
 * 앱 내부에서 사용하는 여행 취향 모델입니다.
 *
 * ERD의 travel_preference를 참고하되,
 * 화면에서는 선호 지역을 여러 개 선택할 수 있으므로 List<Region>으로 둡니다.
 */
data class TravelPreference(
    val userId: String,
    val travelStyleScore: TravelStyleScore,
    val residenceRegion: Region,
    val preferredRegions: List<Region>,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

/**
 * 여행 스타일 점수입니다.
 *
 * 상세 디자인의 자연-도시 성향 슬라이더가 1~7 범위를 사용하므로
 * 잘못된 값이 앱 내부로 들어오지 않도록 제한합니다.
 */
@JvmInline
value class TravelStyleScore(
    val value: Int,
) {
    init {
        require(value in 1..7) { "TravelStyleScore must be between 1 and 7." }
    }
}

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

/**
 * 야단법석 서비스 로그인 완료 후 앱에서 사용하는 결과입니다.
 *
 * 서버에서 발급한 access token과 refresh token은 Repository 구현체가
 * 로컬 저장소에 저장하므로 앱 내부 모델에는 포함하지 않습니다.
 *
 * 화면에서는 신규 회원 여부와 온보딩 완료 여부를 기준으로
 * 다음 화면을 결정합니다.
 */
data class LoginResult(
    val userId: String,
    val isNewUser: Boolean,
    val onboardingCompleted: Boolean,
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
 * 사용자의 거주 지역, 여행 스타일과 복수의 선호 여행 지역을 관리합니다.
 * 거주 지역과 선호 여행 지역은 같은 시도 체계를 사용하지만
 * 선택 가능한 지역 목록은 각각 다릅니다.
 */
data class TravelPreference(
    val userId: String,
    val travelStyleScore: TravelStyleScore,
    val residenceRegion: ProfileRegion,
    val preferredTravelRegions: List<ProfileRegion>,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(residenceRegion.isAvailableForResidence) {
            "Residence region is not available: ${residenceRegion.code}"
        }

        require(
            preferredTravelRegions.all { region ->
                region.isAvailableForPreferredTravel
            },
        ) {
            "Preferred travel regions contain an unavailable region."
        }
    }
}

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

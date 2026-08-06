package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Gender
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import kotlinx.datetime.LocalDate

/**
 * 신규 사용자의 온보딩 정보를 저장하는 Repository 계약입니다.
 */
interface OnboardingRepository {
    /**
     * 온보딩에서 입력한 약관, 기본 정보 및 여행 취향을 저장합니다.
     *
     * 서버 사용자 식별은 Authorization 헤더를 통해 처리하므로
     * 사용자 ID를 별도로 전달하지 않습니다.
     */
    suspend fun saveOnboarding(
        params: SaveOnboardingParams,
    )
}

/**
 * 온보딩에서 입력받은 필수 약관 동의 정보입니다.
 */
data class OnboardingAgreementsParams(
    val serviceTerms: Boolean,
    val privacyPolicy: Boolean,
) {
    /**
     * 모든 필수 약관에 동의했는지 나타냅니다.
     */
    val areRequiredAgreementsAccepted: Boolean
        get() = serviceTerms && privacyPolicy
}

/**
 * 온보딩 완료 요청에 필요한 앱 내부 입력값입니다.
 *
 * Repository 구현체가 이 값을 서버 요청 DTO로 변환합니다.
 */
data class SaveOnboardingParams(
    val agreements: OnboardingAgreementsParams,
    val nickname: String,
    val gender: Gender,
    val birthDate: LocalDate,
    val favoriteTeam: KboTeam,
    val residenceRegion: ProfileRegion,
    val travelStyleScore: TravelStyleScore,
    val preferredTravelRegions: List<ProfileRegion>,
)

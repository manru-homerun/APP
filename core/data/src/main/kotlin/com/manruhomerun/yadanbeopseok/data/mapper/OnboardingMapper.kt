package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.data.repository.OnboardingAgreementsParams
import com.manruhomerun.yadanbeopseok.data.repository.SaveOnboardingParams
import com.manruhomerun.yadanbeopseok.model.Gender
import com.manruhomerun.yadanbeopseok.network.user.dto.OnboardingAgreementsRequestDto
import com.manruhomerun.yadanbeopseok.network.user.dto.OnboardingRequestDto

/**
 * 앱 내부의 온보딩 입력값을 서버 요청 DTO로 변환합니다.
 */
internal fun SaveOnboardingParams.toOnboardingRequestDto(): OnboardingRequestDto =
    OnboardingRequestDto(
        agreements = agreements.toRequestDto(),
        nickname = nickname,
        gender = gender.toRequestCode(),
        birthDate = birthDate.toString(),
        favoriteTeamId = favoriteTeam.serverId,
        residenceRegionCode = residenceRegion.code,
        travelStyleValue = travelStyleScore.value,
        preferredRegionCodes =
            preferredTravelRegions.map { region ->
                region.code
            },
    )

/**
 * 앱 내부의 필수 약관 동의 정보를 서버 요청 DTO로 변환합니다.
 */
private fun OnboardingAgreementsParams.toRequestDto():
    OnboardingAgreementsRequestDto =
    OnboardingAgreementsRequestDto(
        serviceTerms = serviceTerms,
        privacyPolicy = privacyPolicy,
    )

/**
 * 앱의 성별 모델을 서버에서 사용하는 성별 코드로 변환합니다.
 *
 * enum의 name을 직접 전송하지 않고 명시적으로 변환하여
 * 앱 내부 enum 이름이 변경되더라도 서버 요청값을 유지합니다.
 */
private fun Gender.toRequestCode(): String =
    when (this) {
        Gender.MALE -> "MALE"
        Gender.FEMALE -> "FEMALE"
    }

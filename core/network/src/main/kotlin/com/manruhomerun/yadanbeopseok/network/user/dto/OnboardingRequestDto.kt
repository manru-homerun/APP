package com.manruhomerun.yadanbeopseok.network.user.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 온보딩 완료를 요청할 때 사용하는 DTO입니다.
 *
 * @property agreements 가입 단계에서 입력받은 필수 약관 동의 정보
 * @property nickname 사용자가 입력한 닉네임
 * @property gender 사용자가 선택한 성별 코드
 * @property birthday 사용자의 생년월일(yyyy-MM-dd)
 * @property favoriteTeamId 서버에서 사용하는 응원 구단 ID
 * @property residenceRegion 사용자의 거주 지역 이름
 * @property travelStyleValue 자연과 도시 사이의 여행 성향 점수
 * @property preferredRegions 사용자가 선택한 선호 여행 지역 이름 목록
 */
@Serializable
data class OnboardingRequestDto(
    val agreements: OnboardingAgreementsRequestDto,
    val nickname: String,
    val gender: String,
    val birthday: String,
    val favoriteTeamId: Long,
    val residenceRegion: String,
    val travelStyleValue: Int,
    val preferredRegions: List<String>,
)

/**
 * 온보딩 요청에 포함되는 필수 약관 동의 정보입니다.
 *
 * 가입 연령은 [OnboardingRequestDto.birthday]를 기준으로 서버에서 검증합니다.
 *
 * @property serviceTerms 서비스 이용약관 동의 여부
 * @property privacyPolicy 개인정보 수집·이용 동의 여부
 */
@Serializable
data class OnboardingAgreementsRequestDto(
    val serviceTerms: Boolean,
    val privacyPolicy: Boolean,
)

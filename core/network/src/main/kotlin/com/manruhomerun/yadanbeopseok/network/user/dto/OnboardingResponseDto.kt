package com.manruhomerun.yadanbeopseok.network.user.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 온보딩 완료 응답에서 앱이 확인할 데이터입니다.
 *
 * 서버가 반환하는 닉네임, 응원 구단, 여행 취향 등의 추가 필드는
 * 현재 Repository의 온보딩 완료 처리에 필요하지 않으므로 포함하지 않습니다.
 * NetworkModule의 ignoreUnknownKeys 설정을 통해 나머지 필드는 무시합니다.
 *
 * @property userId 온보딩을 완료한 야단법석 사용자 ID
 * @property onboardingCompleted 온보딩이 정상적으로 완료되었는지 여부
 */
@Serializable
data class OnboardingResponseDto(
    val userId: Long,
    val onboardingCompleted: Boolean,
)

package com.manruhomerun.yadanbeopseok.network.user.dto

import kotlinx.serialization.Serializable

/**
 * 닉네임 중복 확인 응답입니다.
 *
 * @property available 입력한 닉네임을 사용할 수 있는지 여부
 */
@Serializable
data class NicknameAvailabilityResponseDto(
    val available: Boolean,
)

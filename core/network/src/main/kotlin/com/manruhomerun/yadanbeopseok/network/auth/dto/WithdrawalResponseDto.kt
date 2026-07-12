package com.manruhomerun.yadanbeopseok.network.auth.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * 야단법석 회원 탈퇴 성공 응답의 data를 변환하는 DTO입니다.
 *
 * @property userId 탈퇴 처리된 야단법석 사용자 ID
 * @property withdrawnAt 서버에서 회원 탈퇴를 처리한 날짜와 시간
 */
@Serializable
data class WithdrawalResponseDto(
    val userId: Long,
    val withdrawnAt: LocalDateTime,
)

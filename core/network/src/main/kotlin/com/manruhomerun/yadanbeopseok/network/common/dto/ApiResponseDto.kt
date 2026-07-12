package com.manruhomerun.yadanbeopseok.network.common.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 백엔드가 공통으로 반환하는 API 응답 형식입니다.
 *
 * @param T API별 응답 데이터 타입
 * @property success 요청 성공 여부
 * @property message 서버에서 전달하는 응답 메시지
 * @property data API별 응답 데이터이며, 데이터가 없는 응답에서는 null입니다.
 */
@Serializable
data class ApiResponseDto<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
)

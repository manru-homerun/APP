package com.manruhomerun.yadanbeopseok.network.common.dto

import kotlinx.serialization.Serializable

/**
 * 야단법석 백엔드가 예외 발생 시 반환하는 공통 응답 형식입니다.
 *
 * 성공 응답에 사용하는 [ApiResponseDto]와 달리,
 * HTTP 4xx 또는 5xx 응답의 error body를 변환할 때 사용합니다.
 *
 * @property code 앱에서 에러 상황을 구분하기 위한 서버 고유 에러 코드
 * @property message 사용자 또는 개발자가 확인할 수 있는 에러 메시지
 * @property path 예외가 발생한 API 요청 경로
 */
@Serializable
data class ApiErrorResponseDto(
    val code: String,
    val message: String,
    val path: String,
)

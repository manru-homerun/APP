package com.manruhomerun.yadanbeopseok.network.common.extension

import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto

/**
 * 데이터가 필요한 성공 응답에서 필수 data를 반환합니다.
 *
 * HTTP 요청이 성공했더라도 success가 false이거나 data가 없다면
 * 백엔드 공통 응답 규격에 맞지 않는 응답으로 처리합니다.
 *
 * @return 응답에 포함된 필수 데이터
 * @throws InvalidResponseException success가 false이거나 data가 없는 경우
 */
fun <T> ApiResponseDto<T>.requireData(): T {
    if (!success) {
        throw InvalidResponseException(message = message)
    }

    return data
        ?: throw InvalidResponseException(
            message = "Response data is missing.",
        )
}

/**
 * 응답 데이터가 필요하지 않은 API의 성공 여부를 확인합니다.
 *
 * HTTP 요청이 성공했더라도 success가 false라면
 * 백엔드 공통 응답 규격에 맞지 않는 응답으로 처리합니다.
 *
 * @throws InvalidResponseException success가 false인 경우
 */
fun ApiResponseDto<*>.requireSuccess() {
    if (!success) {
        throw InvalidResponseException(message = message)
    }
}

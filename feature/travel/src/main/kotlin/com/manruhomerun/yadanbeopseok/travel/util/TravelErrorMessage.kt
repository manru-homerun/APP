package com.manruhomerun.yadanbeopseok.travel.util

import com.manruhomerun.yadanbeopseok.common.error.NetworkConnectionException
import com.manruhomerun.yadanbeopseok.common.error.NetworkTimeoutException

/** 여행 기능의 예외를 내부 오류 정보가 노출되지 않는 안내 문구로 변환합니다. */
internal fun Throwable.toTravelErrorMessage(fallbackMessage: String): String {
    return when (this) {
        is NetworkConnectionException ->
            "인터넷 연결을 확인한 후 다시 시도해주세요."

        is NetworkTimeoutException ->
            "서버 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요."

        else -> "$fallbackMessage 잠시 후 다시 시도해주세요."
    }
}

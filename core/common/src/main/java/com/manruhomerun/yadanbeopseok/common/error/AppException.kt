package com.manruhomerun.yadanbeopseok.common.error

/**
 * 앱 전체에서 공통으로 사용하는 예외의 최상위 타입입니다.
 *
 * 네트워크 모듈의 Retrofit, OkHttp와 같은 구현 타입이
 * ViewModel이나 화면까지 직접 전달되지 않도록 사용합니다.
 */
sealed class AppException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * 백엔드가 HTTP 4xx 또는 5xx로 반환한 오류입니다.
 *
 * @property statusCode HTTP 상태 코드
 * @property errorCode 백엔드에서 정의한 고유 에러 코드
 * @property path 예외가 발생한 API 요청 경로
 */
class ApiException(
    val statusCode: Int,
    val errorCode: String?,
    message: String,
    val path: String?,
    cause: Throwable? = null,
) : AppException(
    message = message,
    cause = cause,
)

/**
 * 인터넷 연결 실패 등 서버와 통신할 수 없을 때 발생합니다.
 */
class NetworkConnectionException(
    cause: Throwable? = null,
) : AppException(
    message = "Network connection failed.",
    cause = cause,
)

/**
 * 서버 요청 시간이 제한 시간을 초과했을 때 발생합니다.
 */
class NetworkTimeoutException(
    cause: Throwable? = null,
) : AppException(
    message = "Network request timed out.",
    cause = cause,
)

/**
 * 서버 응답이 예상한 JSON 형식과 다르거나 필수 데이터가 없을 때 발생합니다.
 */
class InvalidResponseException(
    message: String = "Invalid server response.",
    cause: Throwable? = null,
) : AppException(
    message = message,
    cause = cause,
)

/**
 * refresh token이 만료되거나 유효하지 않아 로그인을 다시 해야 할 때 발생합니다.
 */
class SessionExpiredException(
    cause: Throwable? = null,
) : AppException(
    message = "Session expired.",
    cause = cause,
)

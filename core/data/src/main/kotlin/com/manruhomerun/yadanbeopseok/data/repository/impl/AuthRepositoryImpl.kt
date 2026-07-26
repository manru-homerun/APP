package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.mapper.toAuthTokens
import com.manruhomerun.yadanbeopseok.data.mapper.toLoginResult
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.datastore.AuthTokenDataSource
import com.manruhomerun.yadanbeopseok.model.LoginResult
import com.manruhomerun.yadanbeopseok.network.auth.api.AuthApi
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshRequestDto
import com.manruhomerun.yadanbeopseok.network.common.dto.ApiResponseDto
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.time.Instant
import javax.inject.Inject

/**
 * [AuthRepository]의 실제 구현체입니다.
 *
 * 카카오 SDK에서 받은 카카오 액세스 토큰을 백엔드에 전달하고,
 * 백엔드에서 발급한 야단법석 서비스 토큰을 DataStore에 저장합니다.
 */
internal class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val authTokenDataSource: AuthTokenDataSource,
) : AuthRepository {
    /**
     * 카카오 액세스 토큰으로 야단법석 서비스에 로그인합니다.
     *
     * 로그인에 성공하면 백엔드에서 발급한 access token과 refresh token을
     * DataStore에 저장하고 화면 이동에 필요한 로그인 결과를 반환합니다.
     */
    override suspend fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String?,
    ): LoginResult {
        val response =
            apiCallExecutor.execute {
                authApi.login(
                    request =
                        LoginRequestDto(
                            providerAccessToken = kakaoAccessToken,
                            deviceType = "ANDROID",
                            fcmToken = fcmToken?.takeIf { it.isNotBlank() },
                        ),
                )
            }

        val loginResponse = response.requireData()

        authTokenDataSource.saveAuthTokens(
            authTokens =
                loginResponse.toAuthTokens(
                    currentEpochSeconds = currentEpochSeconds(),
                ),
        )

        return loginResponse.toLoginResult()
    }

    /**
     * DataStore에 저장된 refresh token으로 서비스 토큰을 재발급합니다.
     *
     * refresh token이 없거나 이미 만료된 경우 인증 정보를 삭제하고
     * [SessionExpiredException]을 발생시킵니다.
     *
     * 재발급에 성공하면 access token과 refresh token을 모두 교체합니다.
     */
    override suspend fun refreshAccessToken() {
        
        val storedTokens = authTokenDataSource.getAuthTokens()

        if (storedTokens == null) {
            authTokenDataSource.clearAuthTokens()
            throw SessionExpiredException()
        }
        if (
            storedTokens.refreshTokenExpiresAtEpochSeconds <=
            currentEpochSeconds()
        ) {
            authTokenDataSource.clearAuthTokens()
            throw SessionExpiredException()
        }

        val response =
            try {
                apiCallExecutor.execute {
                    authApi.refreshToken(
                        request =
                            TokenRefreshRequestDto(
                                refreshToken = storedTokens.refreshToken,
                            ),
                    )
                }
            } catch (exception: ApiException) {
                handleAuthenticatedApiException(exception)
            }

        val refreshResponse = response.requireData()

        authTokenDataSource.saveAuthTokens(
            authTokens =
                refreshResponse.toAuthTokens(
                    currentEpochSeconds = currentEpochSeconds(),
                ),
        )
    }

    /**
     * 야단법석 서비스 인증 정보를 DataStore에서 삭제합니다.
     *
     * 백엔드 로그아웃 API가 없으므로 현재 로그아웃은 로컬 토큰 삭제로 처리합니다.
     */
    override suspend fun logout() {
        authTokenDataSource.clearAuthTokens()
    }

    /**
     * 백엔드에 회원 탈퇴를 요청합니다.
     *
     * 서버에서 탈퇴 처리가 완료된 경우에만 로컬 인증 정보를 삭제합니다.
     */
    override suspend fun withdraw() {
        val response =
            try {
                apiCallExecutor.execute {
                    authApi.withdraw()
                }
            } catch (exception: ApiException) {
                handleAuthenticatedApiException(exception)
            }

        response.requireSuccess()
        authTokenDataSource.clearAuthTokens()
    }

    /**
     * 인증이 필요한 요청에서 401 응답을 받은 경우
     * 로컬 인증 정보를 삭제하고 세션 만료 예외로 변환합니다.
     */
    private suspend fun handleAuthenticatedApiException(
        exception: ApiException,
    ): Nothing {
        if (exception.statusCode == HTTP_UNAUTHORIZED) {
            authTokenDataSource.clearAuthTokens()
            throw SessionExpiredException(cause = exception)
        }

        throw exception
    }

    /**
     * 현재 Unix epoch 시간(초)을 반환합니다.
     */
    private fun currentEpochSeconds(): Long =
        Instant.now().epochSecond

}

/**
 * 데이터가 필요한 성공 응답에서 필수 data를 꺼냅니다.
 *
 * HTTP 성공 응답이지만 success가 false이거나 data가 없으면
 * 백엔드 응답 규격에 맞지 않는 것으로 처리합니다.
 */
private fun <T> ApiResponseDto<T>.requireData(): T {
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
 */
private fun ApiResponseDto<*>.requireSuccess() {
    if (!success) {
        throw InvalidResponseException(message = message)
    }
}

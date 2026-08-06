package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.common.error.ApiException
import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.mapper.toAuthTokens
import com.manruhomerun.yadanbeopseok.data.mapper.toLoginResult
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.datastore.AuthTokenDataSource
import com.manruhomerun.yadanbeopseok.model.LoginResult
import com.manruhomerun.yadanbeopseok.network.auth.api.AuthApi
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.LogoutRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshRequestDto
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.common.extension.requireSuccess
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
                            deviceType = ANDROID_DEVICE_TYPE,
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
     * DataStore에 저장된 현재 야단법석 사용자 ID를 조회합니다.
     *
     * 네트워크 요청 없이 로컬 인증 정보에서 사용자 ID를 반환합니다.
     */
    override suspend fun getCurrentUserId(): String? = authTokenDataSource.getCurrentUserId()

    /**
     * DataStore에 저장된 refresh token으로 서비스 토큰을 재발급합니다.
     *
     * refresh token이 없으면 [SessionExpiredException]을 발생시킵니다.
     * refresh token이 이미 만료된 경우에는 인증 정보를 삭제한 후
     * [SessionExpiredException]을 발생시킵니다.
     *
     * 재발급에 성공하면 access token과 refresh token을 모두 교체합니다.
     */
    override suspend fun refreshAccessToken() {
        val storedTokens = authTokenDataSource.getAuthTokens() ?: throw SessionExpiredException()

        if (storedTokens.refreshTokenExpiresAtEpochSeconds <= currentEpochSeconds()) {
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
                    userId = storedTokens.userId,
                    currentEpochSeconds = currentEpochSeconds(),
                ),
        )
    }

    /**
     * 백엔드에 로그아웃을 요청하고 야단법석 인증 정보를 삭제합니다.
     *
     * 서버에서 현재 기기의 FCM 토큰 비활성화를 처리할 수 있도록
     * 카카오 액세스 토큰과 기기 정보를 함께 전달합니다.
     *
     * 서버 로그아웃이 완료된 경우에만 로컬 인증 정보를 삭제합니다.
     */
    override suspend fun logout(
        kakaoAccessToken: String,
        deviceId: String?,
        fcmToken: String?,
    ) {
        val response =
            try {
                apiCallExecutor.execute {
                    authApi.logout(
                        request =
                            LogoutRequestDto(
                                providerAccessToken = kakaoAccessToken,
                                deviceType = ANDROID_DEVICE_TYPE,
                                deviceId = deviceId?.takeIf { it.isNotBlank() },
                                fcmToken = fcmToken?.takeIf { it.isNotBlank() },
                            ),
                    )
                }
            } catch (exception: ApiException) {
                handleAuthenticatedApiException(exception)
            }

        response.requireSuccess()
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

private const val ANDROID_DEVICE_TYPE = "ANDROID"

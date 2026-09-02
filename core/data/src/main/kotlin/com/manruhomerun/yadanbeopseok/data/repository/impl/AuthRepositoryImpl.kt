package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.common.error.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.mapper.toAuthTokens
import com.manruhomerun.yadanbeopseok.data.mapper.toLoginResult
import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.data.repository.AuthSessionState
import com.manruhomerun.yadanbeopseok.datastore.AuthTokenDataSource
import com.manruhomerun.yadanbeopseok.model.LoginResult
import com.manruhomerun.yadanbeopseok.network.auth.api.AuthApi
import com.manruhomerun.yadanbeopseok.network.auth.dto.LoginRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.LogoutRequestDto
import com.manruhomerun.yadanbeopseok.network.auth.dto.TokenRefreshRequestDto
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.common.extension.requireSuccess
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

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
        val response = apiCallExecutor.executeLogin {
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
     * 저장된 서비스 토큰을 확인하여 앱 시작 시 세션을 복원합니다.
     *
     * access token이 유효하면 저장된 세션을 그대로 사용합니다.
     * access token이 만료됐지만 refresh token이 유효하면 토큰을 재발급합니다.
     * 두 토큰 모두 사용할 수 없으면 인증 정보를 삭제하고 로그아웃 상태를 반환합니다.
     *
     * 토큰 재발급 중 발생한 네트워크 오류는 호출자에게 전달하여
     * 앱 시작 화면에서 다시 시도할 수 있도록 합니다.
     */
    override suspend fun restoreSession(): AuthSessionState {
        val storedTokens =
            authTokenDataSource.getAuthTokens()
                ?: return AuthSessionState.LOGGED_OUT

        val currentEpochSeconds = currentEpochSeconds()

        val activeTokens =
            if (storedTokens.accessTokenExpiresAtEpochSeconds > currentEpochSeconds) {
                storedTokens
            } else {
                if (storedTokens.refreshTokenExpiresAtEpochSeconds <= currentEpochSeconds) {
                    authTokenDataSource.clearAuthTokens()
                    return AuthSessionState.LOGGED_OUT
                }

                try {
                    refreshAccessToken()
                } catch (_: SessionExpiredException) {
                    return AuthSessionState.LOGGED_OUT
                }

                authTokenDataSource.getAuthTokens() ?: return AuthSessionState.LOGGED_OUT
            }

        return if (activeTokens.onboardingCompleted) {
            AuthSessionState.AUTHENTICATED
        } else {
            AuthSessionState.ONBOARDING_REQUIRED
        }
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

        val response = apiCallExecutor.execute {
            authApi.refreshToken(
                request = TokenRefreshRequestDto(refreshToken = storedTokens.refreshToken),
            )
        }

        val refreshResponse = response.requireData()

        authTokenDataSource.saveAuthTokens(
            authTokens =
                refreshResponse.toAuthTokens(
                    userId = storedTokens.userId,
                    onboardingCompleted = storedTokens.onboardingCompleted,
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
     * 서버 요청의 성공 여부와 관계없이 로컬 인증 정보를 삭제합니다.
     * 서버 요청에는 삭제 전의 access token이 Authorization 헤더로 사용됩니다.
     */
    override suspend fun logout(
        kakaoAccessToken: String,
        deviceId: String?,
        fcmToken: String?,
    ) {
        try {
            val response = apiCallExecutor.execute {
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

            response.requireSuccess()
        } finally {
            withContext(NonCancellable) {
                authTokenDataSource.clearAuthTokens()
            }
        }
    }

    /**
     * 백엔드에 회원 탈퇴를 요청합니다.
     *
     * 서버에서 탈퇴 처리가 완료된 경우에만 로컬 인증 정보를 삭제합니다.
     */
    override suspend fun withdraw() {
        val response = apiCallExecutor.execute {
            authApi.withdraw()
        }

        response.requireSuccess()
        authTokenDataSource.clearAuthTokens()
    }

    /**
     * 현재 Unix epoch 시간(초)을 반환합니다.
     */
    private fun currentEpochSeconds(): Long =
        Instant.now().epochSecond
}

private const val ANDROID_DEVICE_TYPE = "ANDROID"

package com.manruhomerun.yadanbeopseok.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 야단법석 서비스 인증 정보를 Preferences DataStore에 저장하고 조회합니다.
 *
 * Preferences 키와 저장 방식을 외부에 노출하지 않고,
 * [AuthTokens] 모델을 통해 온보딩 상태와 인증 토큰을 전달합니다.
 */
@Singleton
class AuthTokenDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * 현재 저장된 야단법석 인증 정보를 제공합니다.
     *
     * 저장된 값이 없거나 일부 값이 누락되어 있으면 null을 반환합니다.
     * DataStore 파일을 읽지 못한 경우에는 로그아웃 상태로 처리합니다.
     */
    val authTokens: Flow<AuthTokens?> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences.toAuthTokens()
            }

    /**
     * 로그인 또는 토큰 재발급으로 받은 인증 정보를
     * 하나의 DataStore 수정 작업으로 저장합니다.
     */
    suspend fun saveAuthTokens(authTokens: AuthTokens) {
        dataStore.edit { preferences ->
            preferences[AuthTokenPreferenceKeys.ONBOARDING_COMPLETED] = authTokens.onboardingCompleted
            preferences[AuthTokenPreferenceKeys.ACCESS_TOKEN] = authTokens.accessToken
            preferences[AuthTokenPreferenceKeys.REFRESH_TOKEN] = authTokens.refreshToken
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_USER_ID)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_TOKEN_TYPE)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_ACCESS_TOKEN_EXPIRES_AT)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_REFRESH_TOKEN_EXPIRES_AT)
        }
    }

    /**
     * 현재 저장된 인증 정보를 한 번 조회합니다.
     */
    suspend fun getAuthTokens(): AuthTokens? = authTokens.first()

    /**
     * 현재 사용자의 온보딩 상태를 완료로 변경합니다.
     *
     * 정상적인 인증 정보가 저장된 경우에만 값을 변경하며,
     * 인증 정보가 없다면 false를 반환합니다.
     */
    suspend fun markOnboardingCompleted(): Boolean {
        var updated = false

        dataStore.edit { preferences ->
            if (preferences.toAuthTokens() != null) {
                preferences[AuthTokenPreferenceKeys.ONBOARDING_COMPLETED] = true
                updated = true
            }
        }

        return updated
    }

    /**
     * 로그아웃, 회원 탈퇴 또는 세션 만료 시 인증 정보를 삭제합니다.
     *
     * 같은 Preferences DataStore에 다른 설정값이 추가되더라도
     * 온보딩 상태와 인증 토큰에 해당하는 키만 제거합니다.
     * 이전 인증 구조에서 사용하던 키도 함께 정리합니다.
     */
    suspend fun clearAuthTokens() {
        dataStore.edit { preferences ->
            preferences.remove(AuthTokenPreferenceKeys.ONBOARDING_COMPLETED)
            preferences.remove(AuthTokenPreferenceKeys.ACCESS_TOKEN)
            preferences.remove(AuthTokenPreferenceKeys.REFRESH_TOKEN)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_USER_ID)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_TOKEN_TYPE)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_ACCESS_TOKEN_EXPIRES_AT)
            preferences.remove(AuthTokenPreferenceKeys.LEGACY_REFRESH_TOKEN_EXPIRES_AT)
        }
    }
}

/**
 * 인증 정보 저장에 사용하는 Preferences 키입니다.
 */
private object AuthTokenPreferenceKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("auth_onboarding_completed")
    val ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
    val REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
    val LEGACY_USER_ID = stringPreferencesKey("auth_user_id")
    val LEGACY_TOKEN_TYPE = stringPreferencesKey("auth_token_type")
    val LEGACY_ACCESS_TOKEN_EXPIRES_AT = longPreferencesKey("auth_access_token_expires_at_epoch_seconds")
    val LEGACY_REFRESH_TOKEN_EXPIRES_AT = longPreferencesKey("auth_refresh_token_expires_at_epoch_seconds")
}

/**
 * 모든 인증 값이 정상적으로 저장된 경우에만 [AuthTokens]로 변환합니다.
 */
private fun Preferences.toAuthTokens(): AuthTokens? {
    val onboardingCompleted = this[AuthTokenPreferenceKeys.ONBOARDING_COMPLETED] ?: return null

    val accessToken =
        this[AuthTokenPreferenceKeys.ACCESS_TOKEN]
            ?.takeIf { it.isNotBlank() }
            ?: return null

    val refreshToken =
        this[AuthTokenPreferenceKeys.REFRESH_TOKEN]
            ?.takeIf { it.isNotBlank() }
            ?: return null

    return AuthTokens(
        onboardingCompleted = onboardingCompleted,
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}

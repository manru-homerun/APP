package com.manruhomerun.yadanbeopseok.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AuthTokenDataSourceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `저장된 인증 정보가 없으면 null을 반환한다`() = runTest {
        val fixture = createFixture(backgroundScope)

        assertNull(fixture.dataSource.getAuthTokens())
        assertNull(fixture.dataSource.getCurrentUserId())
    }

    @Test
    fun `인증 정보를 저장하면 동일한 값이 조회된다`() = runTest {
        val fixture = createFixture(backgroundScope)

        fixture.dataSource.saveAuthTokens(INITIAL_AUTH_TOKENS)

        assertEquals(
            expected = INITIAL_AUTH_TOKENS,
            actual = fixture.dataSource.getAuthTokens(),
        )
    }

    @Test
    fun `인증 정보를 저장하면 현재 사용자 ID가 조회된다`() = runTest {
        val fixture = createFixture(backgroundScope)

        fixture.dataSource.saveAuthTokens(INITIAL_AUTH_TOKENS)

        assertEquals(
            expected = INITIAL_AUTH_TOKENS.userId,
            actual = fixture.dataSource.getCurrentUserId(),
        )
    }

    @Test
    fun `재발급 인증 정보를 저장하면 기존 인증 정보 전체를 교체한다`() = runTest {
        val fixture = createFixture(backgroundScope)
        fixture.dataSource.saveAuthTokens(INITIAL_AUTH_TOKENS)

        fixture.dataSource.saveAuthTokens(REFRESHED_AUTH_TOKENS)

        assertEquals(
            expected = REFRESHED_AUTH_TOKENS,
            actual = fixture.dataSource.getAuthTokens(),
        )
    }

    @Test
    fun `인증 정보를 삭제하면 인증 관련 값을 모두 삭제한다`() = runTest {
        val fixture = createFixture(backgroundScope)
        val userIdKey = stringPreferencesKey("auth_user_id")
        val onboardingCompletedKey =
            booleanPreferencesKey("auth_onboarding_completed")

        fixture.dataSource.saveAuthTokens(INITIAL_AUTH_TOKENS)

        fixture.dataSource.clearAuthTokens()

        val preferences = fixture.dataStore.data.first()

        assertNull(fixture.dataSource.getAuthTokens())
        assertNull(fixture.dataSource.getCurrentUserId())
        assertNull(preferences[userIdKey])
        assertNull(preferences[onboardingCompletedKey])
    }

    @Test
    fun `인증 정보가 일부만 저장되어 있으면 null을 반환한다`() = runTest {
        val fixture = createFixture(backgroundScope)
        val accessTokenKey = stringPreferencesKey("auth_access_token")

        fixture.dataStore.edit { preferences ->
            preferences[accessTokenKey] = "partial-access-token"
        }

        assertNull(fixture.dataSource.getAuthTokens())
        assertNull(fixture.dataSource.getCurrentUserId())
    }

    @Test
    fun `인증 정보를 삭제해도 다른 Preferences 값은 유지한다`() = runTest {
        val fixture = createFixture(backgroundScope)
        val notificationEnabledKey =
            booleanPreferencesKey("notification_enabled")

        fixture.dataStore.edit { preferences ->
            preferences[notificationEnabledKey] = true
        }
        fixture.dataSource.saveAuthTokens(INITIAL_AUTH_TOKENS)

        fixture.dataSource.clearAuthTokens()

        val preferences = fixture.dataStore.data.first()
        assertEquals(
            expected = true,
            actual = preferences[notificationEnabledKey],
        )
    }

    @Test
    fun `인증 정보가 있으면 온보딩 완료 상태를 갱신한다`() = runTest {
        val fixture = createFixture(backgroundScope)
        fixture.dataSource.saveAuthTokens(INITIAL_AUTH_TOKENS)

        val updated = fixture.dataSource.markOnboardingCompleted()

        assertEquals(
            expected = true,
            actual = updated,
        )
        assertEquals(
            expected = true,
            actual = fixture.dataSource
                .getAuthTokens()
                ?.onboardingCompleted,
        )
    }

    @Test
    fun `인증 정보가 없으면 온보딩 완료 상태를 갱신하지 않는다`() = runTest {
        val fixture = createFixture(backgroundScope)

        val updated = fixture.dataSource.markOnboardingCompleted()

        assertEquals(
            expected = false,
            actual = updated,
        )
        assertNull(fixture.dataSource.getAuthTokens())
    }

    private fun createFixture(
        scope: CoroutineScope,
    ): TestFixture {
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = {
                    File(
                        temporaryFolder.root,
                        "auth_tokens.preferences_pb",
                    )
                },
            )

        return TestFixture(
            dataStore = dataStore,
            dataSource = AuthTokenDataSource(dataStore),
        )
    }

    private data class TestFixture(
        val dataStore: DataStore<Preferences>,
        val dataSource: AuthTokenDataSource,
    )

    private companion object {
        val INITIAL_AUTH_TOKENS =
            AuthTokens(
                userId = "1",
                onboardingCompleted = false,
                accessToken = "access-token-1",
                refreshToken = "refresh-token-1",
                tokenType = "Bearer",
                accessTokenExpiresAtEpochSeconds = 3_600L,
                refreshTokenExpiresAtEpochSeconds = 1_209_600L,
            )

        val REFRESHED_AUTH_TOKENS =
            AuthTokens(
                userId = "1",
                onboardingCompleted = false,
                accessToken = "access-token-2",
                refreshToken = "refresh-token-2",
                tokenType = "Bearer",
                accessTokenExpiresAtEpochSeconds = 7_200L,
                refreshTokenExpiresAtEpochSeconds = 1_213_200L,
            )
    }
}

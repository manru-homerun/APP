package com.manruhomerun.yadanbeopseok.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 야단법석 앱에서 사용하는 Preferences DataStore입니다.
 *
 * 앱 전체에서 하나의 DataStore 인스턴스만 생성되도록
 * 최상위 Context 확장 프로퍼티로 선언합니다.
 */
private val Context.yadanPreferencesDataStore: DataStore<Preferences> by
preferencesDataStore(
    name = YADAN_PREFERENCES_NAME,
)

/**
 * Preferences DataStore를 애플리케이션 범위의 의존성으로 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    /**
     * 인증 토큰과 추후 추가될 간단한 앱 설정을 저장할
     * Preferences DataStore 인스턴스를 제공합니다.
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        context.yadanPreferencesDataStore
}

/**
 * 실제 기기에 생성되는 Preferences DataStore 파일의 이름입니다.
 */
private const val YADAN_PREFERENCES_NAME = "yadan_preferences"

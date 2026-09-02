package com.manruhomerun.yadanbeopseok.data.di

import com.manruhomerun.yadanbeopseok.data.auth.AuthSessionProviderImpl
import com.manruhomerun.yadanbeopseok.data.auth.TokenRefreshHandlerImpl
import com.manruhomerun.yadanbeopseok.network.auth.token.AuthSessionProvider
import com.manruhomerun.yadanbeopseok.network.auth.token.TokenRefreshHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 인증 네트워크 처리에 필요한 계약과
 * Data 계층 구현체를 연결하는 Hilt 모듈입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AuthDataModule {
    /**
     * Network가 요청하는 [AuthSessionProvider]에
     * [AuthSessionProviderImpl]을 제공합니다.
     */
    @Binds
    @Singleton
    abstract fun bindAuthSessionProvider(
        implementation: AuthSessionProviderImpl
    ): AuthSessionProvider

    /**
     * Network가 요청하는 [TokenRefreshHandler]에
     * [TokenRefreshHandlerImpl]을 제공합니다.
     */
    @Binds
    @Singleton
    abstract fun bindTokenRefreshHandler(
        implementation: TokenRefreshHandlerImpl,
    ): TokenRefreshHandler
}

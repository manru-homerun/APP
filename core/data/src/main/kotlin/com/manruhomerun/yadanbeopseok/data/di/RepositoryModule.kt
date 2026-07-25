package com.manruhomerun.yadanbeopseok.data.di

import com.manruhomerun.yadanbeopseok.data.repository.AuthRepository
import com.manruhomerun.yadanbeopseok.data.repository.impl.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 인터페이스와 실제 구현체를 연결하는 Hilt 모듈입니다.
 *
 * 앱의 다른 계층에서는 구현체를 직접 참조하지 않고
 * Repository 인터페이스를 주입받아 사용합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    /**
     * [AuthRepository] 요청에 [AuthRepositoryImpl]을 제공합니다.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository
}

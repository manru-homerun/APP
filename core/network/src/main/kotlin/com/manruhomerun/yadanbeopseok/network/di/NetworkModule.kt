package com.manruhomerun.yadanbeopseok.network.di

import com.manruhomerun.yadanbeopseok.network.BuildConfig
import com.manruhomerun.yadanbeopseok.network.auth.api.AuthApi
import com.manruhomerun.yadanbeopseok.network.auth.interceptor.AuthInterceptor
import com.manruhomerun.yadanbeopseok.network.auth.interceptor.TokenAuthenticator
import com.manruhomerun.yadanbeopseok.network.baseball.api.BaseballApi
import com.manruhomerun.yadanbeopseok.network.friend.api.FriendApi
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelApi
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelSpotApi
import com.manruhomerun.yadanbeopseok.network.user.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * 야단법석 백엔드 통신에 필요한 네트워크 객체를 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    /**
     * 서버 JSON 응답을 Kotlin DTO로 변환하는 설정을 제공합니다.
     *
     * 서버에 새로운 필드가 추가되어도 기존 앱의 역직렬화가
     * 실패하지 않도록 알 수 없는 필드는 무시합니다.
     */
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
        }

    /**
     * 개발 빌드에서 요청 메서드와 URL 등 기본 통신 정보를 기록합니다.
     *
     * 로그인 토큰이 요청·응답 body에 포함되므로 BODY 로깅은 사용하지 않습니다.
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
        }

    /**
     * Retrofit에서 사용할 OkHttpClient를 제공합니다.
     *
     * [AuthInterceptor]는 일반 요청에 Access Token을 추가하고,
     * [TokenAuthenticator]는 401 응답 시 토큰 재발급과 요청 재시도를 처리합니다.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

    /**
     * 야단법석 백엔드 API 호출에 사용할 Retrofit을 제공합니다.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        json: Json,
        okHttpClient: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_URL.trimEnd('/') + "/")
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            )
            .build()

    /**
     * 인증 관련 백엔드 API 구현체를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit,
    ): AuthApi = retrofit.create(AuthApi::class.java)

    /**
     * 사용자 및 온보딩 관련 백엔드 API 구현체를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideUserApi(
        retrofit: Retrofit,
    ): UserApi = retrofit.create(UserApi::class.java)

    /**
     * 여행 조회 및 생성 관련 백엔드 API 구현체를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideTravelApi(
        retrofit: Retrofit,
    ): TravelApi = retrofit.create(TravelApi::class.java)

    /**
     * 관광지 조회, 검색, 추천 및 찜 관련 백엔드 API 구현체를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideTravelSpotApi(
        retrofit: Retrofit,
    ): TravelSpotApi = retrofit.create(TravelSpotApi::class.java)

    /**
     * KBO 경기 상세와 구단·구장별 경기 일정 API 구현체를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideBaseballApi(
        retrofit: Retrofit,
    ): BaseballApi = retrofit.create(BaseballApi::class.java)

    /**
     * 현재 사용자의 친구 목록 API 구현체를 제공합니다.
     */
    @Provides
    @Singleton
    fun provideFriendApi(
        retrofit: Retrofit
    ): FriendApi = retrofit.create(FriendApi::class.java)
}

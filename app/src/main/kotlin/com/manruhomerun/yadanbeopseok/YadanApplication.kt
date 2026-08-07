package com.manruhomerun.yadanbeopseok

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

/**
 * 야단법석 앱의 전역 초기화를 담당하는 Application 클래스입니다.
 *
 * Hilt 애플리케이션 컴포넌트를 생성하고 Kakao SDK를 초기화합니다.
 */
@HiltAndroidApp
class YadanApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 카카오 로그인 API를 사용하기 전에 Kakao SDK를 한 번 초기화합니다.
        KakaoSdk.init(
            context = this,
            appKey = BuildConfig.KAKAO_NATIVE_APP_KEY,
        )
    }
}

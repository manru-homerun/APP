package com.manruhomerun.yadanbeopseok

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

/**
 * 앱 시작 시 카카오 로그인 SDK와 지도 SDK를 초기화합니다.
 */
@HiltAndroidApp
class YadanApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 카카오 로그인 API를 사용하기 전에 Kakao SDK를 한 번 초기화합니다.
        KakaoSdk.init(context = this, appKey = BuildConfig.KAKAO_NATIVE_APP_KEY)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}

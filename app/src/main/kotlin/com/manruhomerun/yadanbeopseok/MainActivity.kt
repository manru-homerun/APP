package com.manruhomerun.yadanbeopseok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.navigation.rememberYadanNavigationState
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import dagger.hilt.android.AndroidEntryPoint

/**
 * 야단법석 앱의 단일 Activity입니다.
 *
 * 화면 구성은 Compose와 Nav3에 위임하고,
 * Activity는 앱 테마와 최상위 앱 UI만 실행합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            YadanbeopseokTheme {
                YadanbeopseokApp()
            }
        }
    }
}

/**
 * 앱의 시작 NavKey와 최상위 NavHost를 구성합니다.
 */
@Composable
private fun YadanbeopseokApp() {
    val navigationState =
        rememberYadanNavigationState(
            initialKey = LoginNavKey,
        )

    YadanNavHost(
        navigationState = navigationState,
        modifier = Modifier.fillMaxSize(),
    )
}

package com.manruhomerun.yadanbeopseok

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.navigation.rememberYadanNavigationState
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TermsAgreementNavKey
import dagger.hilt.android.AndroidEntryPoint

/**
 * 야단법석 앱의 단일 Activity입니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val startupState by
            viewModel.startupState.collectAsStateWithLifecycle()

            YadanbeopseokTheme {
                val initialNavKey = startupState.toInitialNavKey()

                if (initialNavKey == null) {
                    AppStartupScreen(
                        state = startupState,
                        onRetry = viewModel::restoreSession,
                    )
                } else {
                    key(initialNavKey) {
                        YadanbeopseokApp(
                            initialNavKey = initialNavKey,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 결정된 초기 화면을 기준으로 Nav3 백스택을 생성합니다.
 */
@Composable
private fun YadanbeopseokApp(
    initialNavKey: NavKey,
) {
    val navigationState =
        rememberYadanNavigationState(
            initialKey = initialNavKey,
        )

    YadanNavHost(
        navigationState = navigationState,
        modifier = Modifier.fillMaxSize(),
    )
}

/**
 * 세션 확인 중이거나 확인에 실패했을 때 표시할 화면입니다.
 */
@Composable
private fun AppStartupScreen(
    state: AppStartupState,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
                .fillMaxSize()
                .background(YadanBackground)
                .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            AppStartupState.Checking -> {
                CircularProgressIndicator(
                    modifier =
                        Modifier.semantics {
                            contentDescription = "로그인 정보 확인 중"
                        },
                    color = YadanPrimary,
                )
            }

            AppStartupState.Error -> {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "로그인 정보를 확인하지 못했습니다.",
                        style = YadanTypography.bodyLarge,
                        color = YadanTextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    YadanButton(
                        text = "다시 시도",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            else -> Unit
        }
    }
}

/**
 * 세션 상태를 앱의 최초 NavKey로 변환합니다.
 */
private fun AppStartupState.toInitialNavKey(): NavKey? =
    when (this) {
        AppStartupState.LoginRequired -> LoginNavKey
        AppStartupState.OnboardingRequired -> TermsAgreementNavKey
        AppStartupState.Authenticated -> HomeNavKey
        AppStartupState.Checking,
        AppStartupState.Error,
            -> null
    }

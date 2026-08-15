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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanBottomNavigation
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanBottomNavigationCenterAction
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanBottomNavigationItem
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.MyPageNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelRecordNavKey

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
 * 결정된 초기 화면을 기준으로 Nav3 백스택과
 * 최상위 화면의 하단 내비게이션을 구성합니다.
 */
@Composable
private fun YadanbeopseokApp(
    initialNavKey: NavKey,
) {
    val navigationState =
        rememberYadanNavigationState(
            initialKey = initialNavKey,
        )
    val selectedDestination =
        navigationState.currentTopLevelKey

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = YadanBackground,
        contentWindowInsets =
            WindowInsets(
                left = 0,
                top = 0,
                right = 0,
                bottom = 0,
            ),
        bottomBar = {
            if (navigationState.shouldShowBottomNavigation) {
                Column {
                    YadanBottomNavigation(
                        centerAction = {
                            YadanBottomNavigationCenterAction(
                                onClick = {},
                                enabled = false,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "새 여행 만들기",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        },
                        startItems = {
                            YadanBottomNavigationItem(
                                selected = selectedDestination == HomeNavKey,
                                onClick = {
                                    navigationState.navigateToTopLevel(HomeNavKey,)
                                },
                                label = "홈",
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            YadanBottomNavigationItem(
                                selected = selectedDestination == GameScheduleNavKey,
                                onClick = {
                                    navigationState.navigateToTopLevel(GameScheduleNavKey,)
                                },
                                label = "경기",
                                enabled = false,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsBaseball,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        },
                        endItems = {
                            YadanBottomNavigationItem(
                                selected = selectedDestination == TravelRecordNavKey,
                                onClick = {
                                    navigationState.navigateToTopLevel(TravelRecordNavKey,)
                                },
                                label = "기록",
                                enabled = false,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            YadanBottomNavigationItem(
                                selected = selectedDestination == MyPageNavKey,
                                onClick = {
                                    navigationState.navigateToTopLevel(MyPageNavKey)
                                },
                                label = "마이",
                                enabled = false,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        },
                    )

                    Spacer(
                        modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsBottomHeight(WindowInsets.navigationBars,)
                                .background(YadanSurface),
                    )
                }
            }
        },
    ) { innerPadding ->
        YadanNavHost(
            navigationState = navigationState,
            modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
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

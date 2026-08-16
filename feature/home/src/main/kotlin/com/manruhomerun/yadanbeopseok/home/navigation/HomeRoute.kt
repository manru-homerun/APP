package com.manruhomerun.yadanbeopseok.home.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.home.screen.HomeScreen
import com.manruhomerun.yadanbeopseok.home.viewmodel.HomeNavigationEvent
import com.manruhomerun.yadanbeopseok.home.viewmodel.HomeViewModel
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * 홈 화면과 [HomeViewModel]을 연결합니다.
 *
 * 화면 상태와 사용자 입력은 ViewModel에 연결하고,
 * 홈에서 발생하는 화면 이동과 오류 표시를 처리합니다.
 */
@Composable
fun HomeRoute(
    navigator: Navigator,
    onNotificationClick: () -> Unit,
    onTravelClick: (String) -> Unit,
    onTravelSpotClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    /*
     * 화면이 생성되는 시점의 시스템 지역 기준 날짜입니다.
     * 여행 카드의 진행 상태와 D-Day 계산에 사용합니다.
     */
    val currentDate =
        remember {
            Clock.System.todayIn(
                TimeZone.currentSystemDefault(),
            )
        }

    /*
     * 관광지 상세 등 다른 화면에서 홈으로 돌아오면 데이터를 다시 조회합니다.
     * 상세 화면에서 변경한 찜 상태도 최신 서버 응답으로 동기화됩니다.
     */
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    /*
     * 세션이 만료되면 기존 백스택을 제거하여
     * 뒤로 가기로 인증이 필요한 화면에 돌아오지 않게 합니다.
     */
    LaunchedEffect(viewModel, navigator) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                HomeNavigationEvent.NavigateToLogin -> {
                    navigator.resetTo(LoginNavKey)
                }
            }
        }
    }

    /*
     * ViewModel의 오류를 한 번 표시한 뒤 상태에서 제거합니다.
     */
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage =
            uiState.errorMessage
                ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = errorMessage,
        )
        viewModel.clearErrorMessage()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        HomeScreen(
            uiState = uiState,
            currentDate = currentDate,
            onNotificationClick = onNotificationClick,
            onTravelClick = onTravelClick,
            onGameScheduleClick = {
                navigator.navigateToTopLevel(
                    GameScheduleNavKey,
                )
            },
            onRegionSelected = viewModel::selectRegion,
            onCategorySelected = viewModel::selectCategory,
            onRefreshClick = viewModel::refresh,
            onTravelSpotClick = onTravelSpotClick,
            onDibsClick = viewModel::toggleDibs,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp,
                    ),
        )
    }
}

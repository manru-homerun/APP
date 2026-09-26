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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.home.screen.HomeScreen
import com.manruhomerun.yadanbeopseok.home.viewmodel.HomeViewModel
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.GameScheduleNavKey
import kotlin.time.Clock
import kotlinx.coroutines.delay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * 홈 화면과 [HomeViewModel]을 연결합니다.
 *
 * 화면 상태와 사용자 입력, 홈에서 발생하는 화면 이동과
 * 오류 메시지 표시를 처리합니다.
 *
 * 세션 만료에 따른 로그인 화면 전환은 앱의 공통 세션 관찰이 처리합니다.
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

    /** 여행 카드의 진행 상태와 D-Day 계산에 사용하는 시스템 지역 기준 날짜입니다. */
    var currentDate by remember {
        mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
    var dateScheduleRevision by remember { mutableIntStateOf(0) }

    /*
     * 관광지 상세 등 다른 화면에서 홈으로 돌아오면 데이터를 다시 조회합니다.
     * 상세 화면에서 변경한 찜 상태도 최신 서버 응답으로 동기화됩니다.
     */
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        dateScheduleRevision += 1
        viewModel.refresh()
    }

    /*
     * 화면이 계속 표시된 상태에서도 자정이 지나면 D-Day와 여행 상태를 갱신합니다.
     * 앱 복귀 시 revision이 바뀌어 현재 시스템 시간대 기준으로 예약을 다시 계산합니다.
     */
    LaunchedEffect(dateScheduleRevision) {
        while (true) {
            val timeZone = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val today = Clock.System.todayIn(timeZone)
            val nextMidnight = today
                .plus(1, DateTimeUnit.DAY)
                .atStartOfDayIn(timeZone)
            val delayMillis = (nextMidnight - now)
                .inWholeMilliseconds
                .coerceAtLeast(MIN_DATE_REFRESH_DELAY_MILLIS)

            delay(delayMillis)
            currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        }
    }

    /*
     * ViewModel의 오류를 한 번 표시한 뒤 상태에서 제거합니다.
     */
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect

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
                navigator.navigateToTopLevel(GameScheduleNavKey())
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

private const val MIN_DATE_REFRESH_DELAY_MILLIS = 1_000L

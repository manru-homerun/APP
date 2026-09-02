package com.manruhomerun.yadanbeopseok.travel.course.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.travel.component.TravelNameEditDialog
import com.manruhomerun.yadanbeopseok.travel.course.screen.TravelCourseEditScreen
import com.manruhomerun.yadanbeopseok.travel.course.screen.TravelCourseSavedScreen
import com.manruhomerun.yadanbeopseok.travel.course.screen.TravelCourseSpotSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseEditEvent
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseEditViewModel
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * C01 편집, C01b/C01c 관광지 선택과 C02 저장 완료 화면을 연결합니다.
 *
 * 호출하는 Route에서 진입 경로에 맞게 ViewModel을 초기화해 전달합니다.
 * 내부 화면 전환 중에는 동일한 편집 ViewModel을 사용합니다.
 *
 * @param onExitRequest C01에서 편집 흐름을 종료하려는 요청입니다.
 * 신규 여행의 원본 저장 등 진입 경로별 종료 처리는 호출자가 담당합니다.
 * @param onHomeClick 저장 완료 후 홈으로 이동하는 콜백입니다.
 * @param onTravelSpotClick 관광지 카드 본문을 눌렀을 때의 콜백입니다.
 * @param onSessionExpired 인증 만료 시 로그인 흐름으로 이동하는 콜백입니다.
 */
@Composable
fun TravelCourseEditRoute(
    viewModel: TravelCourseEditViewModel,
    onExitRequest: () -> Unit,
    onHomeClick: () -> Unit,
    onTravelSpotClick: (TravelSpot) -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spotSelectionUiState by viewModel.spotSelectionUiState.collectAsStateWithLifecycle()
    val currentOnSessionExpired by rememberUpdatedState(onSessionExpired)
    val screenStateHolder = rememberSaveableStateHolder()

    val currentDate = remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    var isSaved by rememberSaveable(viewModel) {
        mutableStateOf(false)
    }
    var isNameEditDialogVisible by rememberSaveable(viewModel) {
        mutableStateOf(false)
    }

    var shouldRefreshTravelSpotSelection by rememberSaveable(viewModel) {
        mutableStateOf(false)
    }

    val disabledSpotIds = remember(uiState.course) {
        uiState.course?.days.orEmpty()
            .flatMap { it.places }
            .mapTo(mutableSetOf()) { it.spot.id }
    }

    fun navigateHome() {
        isSaved = false
        isNameEditDialogVisible = false
        viewModel.reset()
        onHomeClick()
    }

    fun navigateBackWithinEdit() {
        if (isSaved) {
            navigateHome()
            return
        }

        if (viewModel.uiState.value.isRequestInProgress) return

        val selection = viewModel.spotSelectionUiState.value

        when {
            selection.isActive && selection.selectionState.isSearchMode -> {
                viewModel.clearTravelSpotSearch()
            }

            selection.isActive -> {
                viewModel.closeTravelSpotSelection()
            }

            else -> {
                onExitRequest()
            }
        }
    }

    /*
     * C01은 Screen 내부에서 드래그·저장 중 뒤로가기를 차단합니다.
     * Route는 관광지 선택과 저장 완료 화면의 시스템 뒤로가기만 처리합니다.
     */
    BackHandler(enabled = isSaved || spotSelectionUiState.isActive) {
        navigateBackWithinEdit()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (shouldRefreshTravelSpotSelection) {
            shouldRefreshTravelSpotSelection = false

            if (spotSelectionUiState.isActive) {
                viewModel.refreshTravelSpotSelection()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                TravelCourseEditEvent.Saved -> {
                    isNameEditDialogVisible = false
                    isSaved = true
                }

                TravelCourseEditEvent.SessionExpired -> {
                    isNameEditDialogVisible = false
                    isSaved = false
                    viewModel.reset()
                    currentOnSessionExpired()
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isSaved -> {
                TravelCourseSavedScreen(
                    travelName = uiState.travelName,
                    travelSpotCount = uiState.travelSpotCount,
                    companionCount = uiState.companionCount,
                    startDate = uiState.startDate,
                    currentDate = currentDate,
                    onHomeClick = ::navigateHome,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            spotSelectionUiState.isActive -> {
                TravelCourseSpotSelectionScreen(
                    uiState = spotSelectionUiState,
                    disabledSpotIds = disabledSpotIds,
                    onSearchQueryChange = viewModel::updateTravelSpotSearchQuery,
                    onSearch = viewModel::searchTravelSpots,
                    onTabSelected = viewModel::selectTravelSpotTab,
                    onCategorySelected = viewModel::selectTravelSpotCategory,
                    onTravelSpotClick = { travelSpot ->
                        shouldRefreshTravelSpotSelection = true
                        onTravelSpotClick(travelSpot)
                    },
                    onTravelSpotToggle = viewModel::toggleTravelSpotSelection,
                    onBackClick = ::navigateBackWithinEdit,
                    onDoneClick = viewModel::confirmTravelSpotSelection,
                    onRetryClick = viewModel::retryTravelSpotSelection,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                // 관광지 선택 화면을 다녀와도 C01의 스크롤 상태를 유지합니다.
                screenStateHolder.SaveableStateProvider(key = "course_edit") {
                    TravelCourseEditScreen(
                        uiState = uiState,
                        onBackClick = ::navigateBackWithinEdit,
                        onRenameClick = {
                            isNameEditDialogVisible = true
                        },
                        onAddPlaceClick = viewModel::openTravelSpotSelection,
                        onRemovePlaceClick = viewModel::removeTravelSpot,
                        onMovePlace = viewModel::moveTravelSpot,
                        onAlignClick = viewModel::alignTravelCourse,
                        onSaveClick = viewModel::saveTravel,
                        onRetryClick = viewModel::retry,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    if (
        isNameEditDialogVisible &&
        !isSaved &&
        !spotSelectionUiState.isActive &&
        uiState.hasContent
    ) {
        TravelNameEditDialog(
            currentName = uiState.travelName,
            onDismiss = {
                isNameEditDialogVisible = false
            },
            onConfirm = { name ->
                viewModel.updateTravelName(name)
                isNameEditDialogVisible = false
            },
        )
    }
}

package com.manruhomerun.yadanbeopseok.travel.creation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey
import com.manruhomerun.yadanbeopseok.travel.component.TravelNameEditDialog
import com.manruhomerun.yadanbeopseok.travel.course.navigation.TravelCourseEditRoute
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseEditViewModel
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelCompanionConditionScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelCompanionSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelCourseGeneratingScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelCourseResultScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelCreationStep
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelDateSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelGameSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelSpotSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.creation.screen.TravelThemeSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.TravelCreationEvent
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.TravelCreationViewModel
import com.manruhomerun.yadanbeopseok.travel.course.screen.TravelCourseSavedScreen
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * B·01부터 B·07까지 여행 만들기 화면과 공유 ViewModel을 연결합니다.
 *
 * 하단 추가 버튼은 B·01부터 시작하고, A·05에서 경기 ID가 전달되면
 * 해당 경기 정보를 조회한 뒤 B·02부터 시작합니다.
 *
 * B·07 원본은 생성 ViewModel에 유지하고, C·01 편집본은 별도 ViewModel에서 관리합니다.
 * 편집본 저장 성공 시 C·02를 표시하고, 편집 종료 요청 시 원본 저장 후 홈으로 이동합니다.
 */
@Composable
fun TravelCreationRoute(
    baseballGameId: String?,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: TravelCreationViewModel = hiltViewModel(),
    editViewModel: TravelCourseEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editUiState by editViewModel.uiState.collectAsStateWithLifecycle()
    val gameSelectionUiState by viewModel.gameSelectionUiState.collectAsStateWithLifecycle()
    val themeSelectionUiState by viewModel.themeSelectionUiState.collectAsStateWithLifecycle()
    val companionSelectionUiState by viewModel.companionSelectionUiState.collectAsStateWithLifecycle()
    val spotSelectionUiState by viewModel.spotSelectionUiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val editStateHolder = rememberSaveableStateHolder()

    val currentDate = remember {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    val directGameId = baseballGameId?.trim()?.takeIf { gameId -> gameId.isNotEmpty() }
    val initialStep = if (directGameId == null) {
        TravelCreationStep.GAME_SELECTION
    } else {
        TravelCreationStep.COMPANION_CONDITION
    }

    var currentStep by rememberSaveable(directGameId) { mutableStateOf(initialStep) }
    var isNameEditDialogVisible by rememberSaveable { mutableStateOf(false) }

    var shouldRefreshTravelSpotSelection by rememberSaveable(directGameId) {
        mutableStateOf(false)
    }

    var isSaved by rememberSaveable {
        mutableStateOf(false)
    }
    var shouldShowSavedScreenAfterSave by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedGame = uiState.selectedGame
    val generatedCourse = uiState.generatedCourse

    val generatedTravelSpotCount = generatedCourse
        ?.days
        .orEmpty()
        .sumOf { day -> day.places.size }

    val selectedStartDate = uiState.startDate
    val selectedEndDate = uiState.endDate
    val isRequestInProgress = uiState.isGenerating || uiState.isSaving
    val isEditing = editUiState.hasContent
    val isDirectGameLoading = directGameId != null &&
        selectedGame == null &&
        gameSelectionUiState.errorMessage == null

    fun finishAtHome() {
        isSaved = false
        shouldShowSavedScreenAfterSave = false
        viewModel.resetCreation()

        // 현재 화면을 제거하고 홈 탭의 시작 화면으로 이동합니다.
        navigator.replaceCurrent(HomeNavKey)
    }

    fun navigateToLogin() {
        viewModel.resetCreation()
        navigator.resetTo(LoginNavKey)
    }

    fun closeCreation() {
        viewModel.resetCreation()
        navigator.navigateBack()
    }

    fun navigateBackWithinCreation() {
        if (isRequestInProgress) return

        if (isSaved) {
            finishAtHome()
            return
        }

        when {
            generatedCourse != null -> {
                viewModel.clearGeneratedCourse()
                currentStep = TravelCreationStep.SPOT_SELECTION
            }

            currentStep == TravelCreationStep.SPOT_SELECTION &&
                spotSelectionUiState.isSearchMode -> {
                viewModel.clearTravelSpotSearch()
            }

            currentStep == TravelCreationStep.GAME_SELECTION -> {
                closeCreation()
            }

            currentStep == TravelCreationStep.COMPANION_CONDITION &&
                directGameId != null -> {
                closeCreation()
            }

            currentStep == TravelCreationStep.COMPANION_CONDITION -> {
                currentStep = TravelCreationStep.GAME_SELECTION
            }

            currentStep == TravelCreationStep.THEME_SELECTION -> {
                currentStep = TravelCreationStep.COMPANION_CONDITION
            }

            currentStep == TravelCreationStep.COMPANION_SELECTION -> {
                currentStep = TravelCreationStep.THEME_SELECTION
            }

            currentStep == TravelCreationStep.DATE_SELECTION -> {
                currentStep = TravelCreationStep.COMPANION_SELECTION
            }

            currentStep == TravelCreationStep.SPOT_SELECTION -> {
                currentStep = TravelCreationStep.DATE_SELECTION
            }
        }
    }

    // 편집 중에는 C01 Route와 Screen이 뒤로가기를 처리합니다.
    BackHandler(enabled = !isEditing || uiState.isSaving) {
        navigateBackWithinCreation()
    }

    LaunchedEffect(directGameId, viewModel) {
        if (directGameId == null) {
            val initialTeam = KboTeam.entries.minBy { team -> team.serverId }
            viewModel.initializeGameSelection(initialTeam)
        } else {
            viewModel.initializeSelectedGame(directGameId)
        }
    }

    LaunchedEffect(currentStep, viewModel) {
        when (currentStep) {
            TravelCreationStep.THEME_SELECTION -> {
                viewModel.initializeThemeSelection()
            }

            TravelCreationStep.COMPANION_SELECTION -> {
                viewModel.initializeCompanionSelection()
            }

            TravelCreationStep.SPOT_SELECTION -> {
                viewModel.initializeTravelSpotSelection()
            }

            else -> Unit
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (shouldRefreshTravelSpotSelection) {
            shouldRefreshTravelSpotSelection = false

            val isSpotSelectionVisible =
                currentStep == TravelCreationStep.SPOT_SELECTION &&
                    generatedCourse == null &&
                    !isEditing &&
                    !isSaved

            if (isSpotSelectionVisible) {
                viewModel.refreshTravelSpotSelection()
            }
        }
    }

    LaunchedEffect(viewModel, navigator) {
        viewModel.events.collect { event ->
            when (event) {
                TravelCreationEvent.GameSelected -> {
                    currentStep = TravelCreationStep.COMPANION_CONDITION
                }

                TravelCreationEvent.CourseGenerated -> {
                    isNameEditDialogVisible = false
                }

                TravelCreationEvent.TravelSaved -> {
                    isNameEditDialogVisible = false

                    if (shouldShowSavedScreenAfterSave) {
                        shouldShowSavedScreenAfterSave = false
                        isSaved = true
                    } else {
                        finishAtHome()
                    }
                }

                TravelCreationEvent.SessionExpired -> {
                    navigateToLogin()
                }
            }
        }
    }

    LaunchedEffect(directGameId, gameSelectionUiState.errorMessage) {
        val gameId = directGameId ?: return@LaunchedEffect
        val errorMessage = gameSelectionUiState.errorMessage ?: return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = errorMessage,
            actionLabel = "다시 시도",
            duration = SnackbarDuration.Indefinite,
        )

        if (result == SnackbarResult.ActionPerformed) {
            viewModel.initializeSelectedGame(gameId)
        }
    }

    LaunchedEffect(uiState.errorMessage, generatedCourse, isEditing) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (generatedCourse != null && !isEditing) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {

            isSaved -> {
                TravelCourseSavedScreen(
                    travelName = uiState.travelName,
                    travelSpotCount = generatedTravelSpotCount,
                    companionCount = uiState.selectedCompanions.size,
                    startDate = selectedStartDate,
                    currentDate = currentDate,
                    onHomeClick = ::finishAtHome,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            isEditing && uiState.isSaving -> {
                TravelRequestLoadingOverlay(
                    message = "원래 여행 일정을 저장하고 있어요",
                    modifier = Modifier.fillMaxSize(),
                )
            }

            isEditing -> {
                // 원본 저장 실패로 편집 화면을 다시 표시해도 스크롤 상태를 복원합니다.
                editStateHolder.SaveableStateProvider(key = "course_edit_flow") {
                    TravelCourseEditRoute(
                        viewModel = editViewModel,
                        onExitRequest = {
                            shouldShowSavedScreenAfterSave = false
                            viewModel.saveTravel()
                        },
                        onHomeClick = ::finishAtHome,
                        onTravelSpotClick = { travelSpot ->
                            navigator.navigate(TravelSpotDetailNavKey(travelSpot.id))
                        },
                        onSessionExpired = ::navigateToLogin,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            uiState.isGenerating -> {
                TravelCourseGeneratingScreen(
                    regionName = selectedGame?.stadium?.region?.displayName.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            generatedCourse != null &&
                selectedGame != null &&
                selectedStartDate != null &&
                selectedEndDate != null -> {
                TravelCourseResultScreen(
                    course = generatedCourse,
                    game = selectedGame,
                    travelName = uiState.travelName,
                    startDate = selectedStartDate,
                    endDate = selectedEndDate,
                    isSaving = uiState.isSaving,
                    onBackClick = ::navigateBackWithinCreation,
                    onRenameClick = {
                        isNameEditDialogVisible = true
                    },
                    onEditScheduleClick = {
                        val params = viewModel.getCurrentCreateTravelParams()
                        if (params != null && !viewModel.uiState.value.isSaving) {
                            viewModel.clearErrorMessage()
                            editViewModel.initializeNewTravel(params, selectedGame)
                        }
                    },
                    onSaveClick = {
                        shouldShowSavedScreenAfterSave = true
                        viewModel.saveTravel()
                    },
                    modifier = Modifier.fillMaxSize(),
                    errorMessage = uiState.errorMessage,
                )
            }

            currentStep == TravelCreationStep.GAME_SELECTION -> {
                TravelGameSelectionScreen(
                    uiState = gameSelectionUiState,
                    onCloseClick = ::closeCreation,
                    onTeamSelected = viewModel::selectScheduleTeam,
                    onGameSelected = viewModel::selectGameSummary,
                    onNextClick = viewModel::confirmSelectedGame,
                    onRetryClick = viewModel::retryGameSchedule,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            currentStep == TravelCreationStep.COMPANION_CONDITION -> {
                TravelCompanionConditionScreen(
                    selectedConditions = uiState.selectedCompanionConditions,
                    isNextEnabled = selectedGame != null && !isDirectGameLoading,
                    onConditionClick = viewModel::toggleCompanionCondition,
                    onBackClick = ::navigateBackWithinCreation,
                    onNextClick = {
                        currentStep = TravelCreationStep.THEME_SELECTION
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            currentStep == TravelCreationStep.THEME_SELECTION -> {
                TravelThemeSelectionScreen(
                    uiState = themeSelectionUiState,
                    selectedThemes = uiState.selectedThemes,
                    onThemeClick = viewModel::toggleTheme,
                    onBackClick = ::navigateBackWithinCreation,
                    onNextClick = {
                        currentStep = TravelCreationStep.COMPANION_SELECTION
                    },
                    onRetryClick = viewModel::retryThemeSelection,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            currentStep == TravelCreationStep.COMPANION_SELECTION -> {
                TravelCompanionSelectionScreen(
                    uiState = companionSelectionUiState,
                    selectedCompanions = uiState.selectedCompanions,
                    onCompanionClick = viewModel::toggleCompanion,
                    onBackClick = ::navigateBackWithinCreation,
                    onNextClick = {
                        currentStep = TravelCreationStep.DATE_SELECTION
                    },
                    onRetryClick = viewModel::retryCompanionSelection,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            currentStep == TravelCreationStep.DATE_SELECTION && selectedGame != null -> {
                TravelDateSelectionScreen(
                    selectedGame = selectedGame,
                    startDate = selectedStartDate,
                    endDate = selectedEndDate,
                    onDateRangeSelected = viewModel::selectDateRange,
                    onBackClick = ::navigateBackWithinCreation,
                    onNextClick = {
                        currentStep = TravelCreationStep.SPOT_SELECTION
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            currentStep == TravelCreationStep.SPOT_SELECTION -> {
                TravelSpotSelectionScreen(
                    uiState = spotSelectionUiState,
                    selectedTravelSpots = uiState.selectedTravelSpots,
                    onSearchQueryChange = viewModel::updateTravelSpotSearchQuery,
                    onSearch = viewModel::searchTravelSpots,
                    onSearchDoneClick = viewModel::clearTravelSpotSearch,
                    onTabSelected = viewModel::selectTravelSpotTab,
                    onCategorySelected = viewModel::selectTravelSpotCategory,
                    onTravelSpotClick = { travelSpot ->
                        shouldRefreshTravelSpotSelection = true
                        navigator.navigate(TravelSpotDetailNavKey(travelSpot.id))
                    },
                    onTravelSpotToggle = viewModel::toggleTravelSpot,
                    onBackClick = ::navigateBackWithinCreation,
                    onGenerateClick = viewModel::generateTravelCourse,
                    onRetryClick = viewModel::retryTravelSpotSelection,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (isDirectGameLoading) {
            TravelRequestLoadingOverlay(modifier = Modifier.fillMaxSize())
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }

    if (isNameEditDialogVisible && !isEditing && !uiState.isSaving) {
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

/**
 * 경기 상세 조회 또는 원본 일정 저장 중 표시하는 공통 대기 UI입니다.
 */
@Composable
private fun TravelRequestLoadingOverlay(
    modifier: Modifier = Modifier,
    message: String = "경기 정보를 확인하고 있어요",
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .background(YadanBackground.copy(alpha = 0.94f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
            )
            .clearAndSetSemantics {
                contentDescription = message
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = YadanPrimary)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = message,
                style = YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = YadanTextPrimary,
            )
        }
    }
}

package com.manruhomerun.yadanbeopseok.travel.navigation

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTextField
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey
import com.manruhomerun.yadanbeopseok.travel.screen.TravelCompanionConditionScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelCompanionSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelCourseGeneratingScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelCourseResultScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelCreationStep
import com.manruhomerun.yadanbeopseok.travel.screen.TravelDateSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelGameSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelSpotSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.screen.TravelThemeSelectionScreen
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelCreationEvent
import com.manruhomerun.yadanbeopseok.travel.viewmodel.TravelCreationViewModel

/**
 * B·01부터 B·07까지 여행 만들기 화면과 공유 ViewModel을 연결합니다.
 *
 * 하단 추가 버튼은 B·01부터 시작하고, A·05에서 경기 ID가 전달되면
 * 해당 경기 정보를 조회한 뒤 B·02부터 시작합니다.
 */
@Composable
fun TravelCreationRoute(
    baseballGameId: String?,
    navigator: Navigator,
    onEditScheduleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: TravelCreationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gameSelectionUiState by viewModel.gameSelectionUiState.collectAsStateWithLifecycle()
    val themeSelectionUiState by viewModel.themeSelectionUiState.collectAsStateWithLifecycle()
    val companionSelectionUiState by viewModel.companionSelectionUiState.collectAsStateWithLifecycle()
    val spotSelectionUiState by viewModel.spotSelectionUiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val directGameId = baseballGameId?.trim()?.takeIf { gameId -> gameId.isNotEmpty() }
    val initialStep = if (directGameId == null) {
        TravelCreationStep.GAME_SELECTION
    } else {
        TravelCreationStep.COMPANION_CONDITION
    }

    var currentStep by rememberSaveable(directGameId) {
        mutableStateOf(initialStep)
    }
    var isNameEditDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val selectedGame = uiState.selectedGame
    val generatedCourse = uiState.generatedCourse
    val selectedStartDate = uiState.startDate
    val selectedEndDate = uiState.endDate
    val isRequestInProgress = uiState.isGenerating || uiState.isSaving
    val isDirectGameLoading = directGameId != null &&
        selectedGame == null &&
        gameSelectionUiState.errorMessage == null

    fun closeCreation() {
        viewModel.resetCreation()
        navigator.navigateBack()
    }

    fun navigateBackWithinCreation() {
        if (isRequestInProgress) return

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

    BackHandler {
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
                    viewModel.resetCreation()
                    navigator.replaceCurrent(HomeNavKey)
                }

                TravelCreationEvent.SessionExpired -> {
                    viewModel.resetCreation()
                    navigator.resetTo(LoginNavKey)
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

    LaunchedEffect(uiState.errorMessage, generatedCourse) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (generatedCourse != null) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isGenerating -> {
                TravelCourseGeneratingScreen(
                    regionName = selectedGame?.stadium?.region?.displayName.orEmpty(),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            generatedCourse != null &&
                selectedStartDate != null &&
                selectedEndDate != null -> {
                TravelCourseResultScreen(
                    course = generatedCourse,
                    travelName = uiState.travelName,
                    startDate = selectedStartDate,
                    endDate = selectedEndDate,
                    isSaving = uiState.isSaving,
                    onBackClick = ::navigateBackWithinCreation,
                    onRenameClick = {
                        isNameEditDialogVisible = true
                    },
                    onEditScheduleClick = onEditScheduleClick,
                    onSaveClick = viewModel::saveTravel,
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
            DirectGameLoadingOverlay(modifier = Modifier.fillMaxSize())
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
        )
    }

    if (isNameEditDialogVisible) {
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
 * A·05에서 전달된 경기 상세 정보를 조회하는 동안 B·02 위에 표시합니다.
 */
@Composable
private fun DirectGameLoadingOverlay(modifier: Modifier = Modifier) {
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
                contentDescription = "선택한 경기 정보 불러오는 중"
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
                text = "경기 정보를 확인하고 있어요",
                style = YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = YadanTextPrimary,
            )
        }
    }
}

/**
 * B·07에서 최종 저장할 여행 이름을 변경하는 대화상자입니다.
 */
@Composable
private fun TravelNameEditDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draftName by rememberSaveable(currentName) {
        mutableStateOf(currentName)
    }

    val normalizedName = draftName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "여행 이름 변경",
                style = YadanTypography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        },
        text = {
            YadanTextField(
                value = draftName,
                onValueChange = { name ->
                    draftName = name
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "여행 이름을 입력해주세요",
                clearContentDescription = "여행 이름 지우기",
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(normalizedName)
                },
                enabled = normalizedName.isNotEmpty(),
            ) {
                Text(text = "변경")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
    )
}

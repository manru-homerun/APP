package com.manruhomerun.yadanbeopseok.travel.detail.navigation

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey
import com.manruhomerun.yadanbeopseok.travel.course.navigation.TravelCourseEditRoute
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseEditViewModel
import com.manruhomerun.yadanbeopseok.travel.detail.screen.TravelDetailScreen
import com.manruhomerun.yadanbeopseok.travel.detail.viewmodel.TravelDetailNavigationEvent
import com.manruhomerun.yadanbeopseok.travel.detail.viewmodel.TravelDetailViewModel
import com.manruhomerun.yadanbeopseok.travel.share.openTravelPosterShareSheet
import com.manruhomerun.yadanbeopseok.travel.share.saveTravelPosterImage
import com.manruhomerun.yadanbeopseok.travel.share.screen.TravelShareScreen
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

/**
 * 여행 상세, 일정 편집과 이미지 저장 화면을 연결합니다.
 *
 * C04에서 일정 편집을 누르면 C01을 표시하고,
 * 이미지로 공유를 누르면 C03을 표시합니다.
 */
@Composable
fun TravelDetailRoute(
    travelId: String,
    navigator: Navigator,
    onVerifyClick: ((TravelPlace) -> Unit)? = null,
    onRenameClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: TravelDetailViewModel = hiltViewModel(),
    editViewModel: TravelCourseEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editUiState by editViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val detailStateHolder = rememberSaveableStateHolder()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val posterGraphicsLayer = rememberGraphicsLayer()

    var isShareVisible by rememberSaveable(travelId) {
        mutableStateOf(false)
    }

    var isSavingPoster by remember(travelId) {
        mutableStateOf(false)
    }

    val isEditing = editUiState.isExistingTravel
    val travel = uiState.travel
    val baseballGame = uiState.baseballGame
    val canOpenShare = travel != null && baseballGame != null

    fun exitEdit() {
        editViewModel.reset()
    }

    fun navigateToLogin() {
        isShareVisible = false
        editViewModel.reset()
        navigator.resetTo(LoginNavKey)
    }

    fun closeShare() {
        isShareVisible = false
    }

    fun saveAndSharePoster() {
        if (isSavingPoster) return

        coroutineScope.launch {
            isSavingPoster = true
            var feedbackMessage: String? = null

            try {
                val imageBitmap = posterGraphicsLayer.toImageBitmap()
                val imageUri = saveTravelPosterImage(
                    context = context,
                    imageBitmap = imageBitmap,
                )

                try {
                    openTravelPosterShareSheet(
                        context = context,
                        imageUri = imageUri,
                    )
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (_: Exception) {
                    feedbackMessage = "이미지는 저장됐지만 공유 화면을 열지 못했습니다."
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: Exception) {
                feedbackMessage = "이미지를 저장하지 못했습니다. 다시 시도해 주세요."
            } finally {
                isSavingPoster = false
            }

            feedbackMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    BackHandler(enabled = isShareVisible && !isEditing) {
        if (!isSavingPoster) {
            closeShare()
        }
    }
    LaunchedEffect(isShareVisible, canOpenShare) {
        if (isShareVisible && !canOpenShare) {
            closeShare()
        }
    }

    LaunchedEffect(travelId, viewModel) {
        viewModel.loadTravel(travelId)
    }

    LaunchedEffect(viewModel, navigator) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                TravelDetailNavigationEvent.NavigateToLogin -> {
                    navigateToLogin()
                }
            }
        }
    }

    /*
     * 최초 상세 조회 실패는 오류 화면으로 표시합니다.
     * 이미 여행 정보가 표시된 상태의 오류만 Snackbar로 안내합니다.
     */
    LaunchedEffect(uiState.errorMessage, uiState.hasTravel) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        if (!uiState.hasTravel) return@LaunchedEffect

        snackbarHostState.showSnackbar(errorMessage)
        viewModel.clearErrorMessage()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isEditing -> {
                TravelCourseEditRoute(
                    viewModel = editViewModel,
                    onExitRequest = ::exitEdit,
                    onHomeClick = {
                        navigator.navigateToTopLevel(HomeNavKey)
                    },
                    onTravelSpotClick = { travelSpot ->
                        navigator.navigate(
                            TravelSpotDetailNavKey(travelSpot.id),
                        )
                    },
                    onSessionExpired = ::navigateToLogin,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            isShareVisible && travel != null && baseballGame != null -> {
                TravelShareScreen(
                    travel = travel,
                    baseballGame = baseballGame,
                    onBackClick = ::closeShare,
                    onSaveImageClick = ::saveAndSharePoster,
                    posterModifier = Modifier.drawWithContent {
                        posterGraphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(posterGraphicsLayer)
                    },
                    isSaving = isSavingPoster,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                /*
                 * C01이나 C03을 다녀온 뒤에도 C04의 선택 일차와
                 * 스크롤 상태를 그대로 복원합니다.
                 */
                detailStateHolder.SaveableStateProvider(key = "travel_detail") {
                    TravelDetailScreen(
                        uiState = uiState,
                        onBackClick = navigator::navigateBack,
                        onDaySelected = viewModel::selectDay,
                        onVerifyClick = onVerifyClick,
                        onRetryClick = viewModel::retry,
                        onRenameClick = onRenameClick,
                        onEditScheduleClick = {
                            editViewModel.initializeExistingTravel(travelId)
                        },
                        onShareImageClick = if (canOpenShare) {
                            {
                                isShareVisible = true
                            }
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
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
}

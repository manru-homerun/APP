package com.manruhomerun.yadanbeopseok.travel.detail.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.designsystem.theme.yadanBackwardTransition
import com.manruhomerun.yadanbeopseok.designsystem.theme.yadanFadeTransition
import com.manruhomerun.yadanbeopseok.designsystem.theme.yadanForwardTransition
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TravelSpotDetailNavKey
import com.manruhomerun.yadanbeopseok.travel.course.navigation.TravelCourseEditRoute
import com.manruhomerun.yadanbeopseok.travel.course.viewmodel.TravelCourseEditViewModel
import com.manruhomerun.yadanbeopseok.travel.detail.screen.TravelDetailScreen
import com.manruhomerun.yadanbeopseok.travel.detail.viewmodel.TravelDetailViewModel
import com.manruhomerun.yadanbeopseok.travel.share.captureAndSaveTravelPosterImage
import com.manruhomerun.yadanbeopseok.travel.share.openTravelPosterShareSheet
import com.manruhomerun.yadanbeopseok.travel.share.screen.TravelShareScreen
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
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
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val graphicsContext = LocalGraphicsContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coroutineScope = rememberCoroutineScope()
    val posterGraphicsLayer = rememberGraphicsLayer()

    var isShareVisible by rememberSaveable(travelId) {
        mutableStateOf(false)
    }

    var isSavingPoster by remember(travelId) {
        mutableStateOf(false)
    }
    var posterSaveJob by remember(travelId) { mutableStateOf<Job?>(null) }

    val isEditing = editUiState.isExistingTravel
    val travel = uiState.travel
    val baseballGame = uiState.baseballGame
    val canOpenShare = travel != null && baseballGame != null
    val currentPage = when {
        isEditing -> TravelDetailPage.EDIT
        isShareVisible && canOpenShare -> TravelDetailPage.SHARE
        else -> TravelDetailPage.DETAIL
    }
    val activeShareTravelId by rememberUpdatedState(travelId.takeIf { currentPage == TravelDetailPage.SHARE })
    var recordedPosterSize by remember(
        travelId,
        isShareVisible,
        travel,
        baseballGame,
        density.density,
        density.fontScale,
        layoutDirection,
    ) { mutableStateOf<IntSize?>(null) }
    val isPosterReady = recordedPosterSize?.let { size ->
        size.width > 0 && size.height > 0 && size == posterGraphicsLayer.size
    } == true

    fun exitEdit() {
        editViewModel.reset()
    }

    fun closeShare() {
        posterSaveJob?.cancel()
        isShareVisible = false
    }

    /** 현재 포스터만 저장하며 화면 이탈이나 취소 후에는 공유창을 열지 않습니다. */
    fun saveAndSharePoster() {
        if (isSavingPoster || !isPosterReady || currentPage != TravelDetailPage.SHARE) return

        val savingTravelId = travelId
        posterSaveJob?.cancel()
        posterSaveJob = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            isSavingPoster = true
            var feedbackMessage: String? = null

            try {
                ensureActive()
                val imageUri = captureAndSaveTravelPosterImage(
                    context = context,
                    graphicsContext = graphicsContext,
                    posterGraphicsLayer = posterGraphicsLayer,
                    density = density,
                    layoutDirection = layoutDirection,
                )
                ensureActive()
                if (activeShareTravelId != savingTravelId || !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    return@launch
                }

                try {
                    openTravelPosterShareSheet(
                        context = context,
                        imageUri = imageUri,
                    )
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (_: OutOfMemoryError) {
                    feedbackMessage = "이미지는 저장됐지만 공유 화면을 열지 못했습니다."
                } catch (_: Exception) {
                    feedbackMessage = "이미지는 저장됐지만 공유 화면을 열지 못했습니다."
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: OutOfMemoryError) {
                feedbackMessage = "이미지를 저장할 메모리가 부족합니다. 다른 앱을 닫고 다시 시도해 주세요."
            } catch (_: Exception) {
                feedbackMessage = "이미지를 저장하지 못했습니다. 다시 시도해 주세요."
            } finally {
                isSavingPoster = false
            }

            ensureActive()
            if (activeShareTravelId != savingTravelId) return@launch
            feedbackMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    DisposableEffect(travelId) {
        onDispose { posterSaveJob?.cancel() }
    }
    LaunchedEffect(travelId, currentPage) {
        if (currentPage != TravelDetailPage.SHARE) posterSaveJob?.cancel()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        posterSaveJob?.cancel()
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

    /**
     * 방문 인증 화면에서 돌아오면 여행을 다시 조회하여
     * 인증 진행률과 관광지별 인증 상태를 갱신합니다.
     */
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshTravel()
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
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                when {
                    initialState == TravelDetailPage.DETAIL -> yadanForwardTransition()
                    targetState == TravelDetailPage.DETAIL -> yadanBackwardTransition()
                    else -> yadanFadeTransition()
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "travel_detail_screen",
        ) { page ->
            when (page) {
                TravelDetailPage.EDIT -> {
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
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                TravelDetailPage.SHARE -> {
                    if (travel != null && baseballGame != null) {
                        TravelShareScreen(
                            travel = travel,
                            baseballGame = baseballGame,
                            onBackClick = ::closeShare,
                            onSaveImageClick = ::saveAndSharePoster,
                            posterModifier = Modifier
                                .onSizeChanged { size ->
                                    if (recordedPosterSize != size) recordedPosterSize = null
                                }
                                .drawWithContent {
                                    posterGraphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }

                                    recordedPosterSize = posterGraphicsLayer.size
                                    drawLayer(posterGraphicsLayer)
                                },
                            isSaving = isSavingPoster,
                            isPosterReady = isPosterReady,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                TravelDetailPage.DETAIL -> {
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

private enum class TravelDetailPage {
    DETAIL,
    EDIT,
    SHARE,
}

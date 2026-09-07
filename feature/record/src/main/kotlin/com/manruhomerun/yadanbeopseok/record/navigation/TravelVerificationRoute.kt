package com.manruhomerun.yadanbeopseok.record.navigation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey
import com.manruhomerun.yadanbeopseok.record.location.CurrentLocationResult
import com.manruhomerun.yadanbeopseok.record.screen.TravelStickerRewardScreen
import com.manruhomerun.yadanbeopseok.record.screen.TravelVerificationCompletedScreen
import com.manruhomerun.yadanbeopseok.record.screen.TravelVerificationScreen
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationPhase
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelVerificationViewModel
import kotlinx.coroutines.delay

/**
 * D02 방문 인증, D02b 인증 완료와 D03 스티커 획득 화면을 연결합니다.
 *
 * 위치 권한과 위치 설정을 처리하고, 인증 완료 후 전체 인증 여부에 따라
 * 일정 화면 또는 스티커 획득 화면으로 전환합니다.
 *
 * @param travelId 인증 대상 여행 ID
 * @param spotId 인증 대상 관광지 ID
 * @param navigator 뒤로가기와 홈 이동에 사용하는 Navigator
 * @param onDecoratePhotoClick D03에서 D04로 이동하는 콜백입니다.
 * null이면 사진에 붙이기 버튼을 비활성화합니다.
 */
@Composable
fun TravelVerificationRoute(
    travelId: String,
    spotId: String,
    navigator: Navigator,
    onDecoratePhotoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: TravelVerificationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    var hasRequestedLocationPermission by rememberSaveable(
        travelId,
        spotId,
    ) {
        mutableStateOf(false)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshLocation()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshLocation()
    }

    /** 위치 권한을 다시 요청할 수 없을 때 앱의 시스템 설정 화면을 엽니다. */
    fun openApplicationSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        )

        settingsLauncher.launch(intent)
    }

    /**
     * 위치 권한을 요청합니다.
     *
     * 권한을 영구적으로 거부한 상태라면 시스템 권한창 대신 앱 설정을 엽니다.
     */
    fun requestLocationPermission() {
        val canRequestAgain = activity == null ||
            locationPermissions.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission,
                )
            }

        if (hasRequestedLocationPermission && !canRequestAgain) {
            openApplicationSettings()
            return
        }

        hasRequestedLocationPermission = true
        permissionLauncher.launch(locationPermissions)
    }

    /** 기기의 위치 서비스가 꺼져 있을 때 시스템 위치 설정 화면을 엽니다. */
    fun openLocationSettings() {
        settingsLauncher.launch(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
        )
    }

    val isBackBlocked = when (uiState.phase) {
        TravelVerificationPhase.SUBMITTING,
        TravelVerificationPhase.REFRESHING_TRAVEL,
        TravelVerificationPhase.VERIFIED,
        TravelVerificationPhase.LOADING_STICKERS -> true

        else -> false
    }

    BackHandler(enabled = isBackBlocked) {
        // 인증 저장과 자동 화면 전환이 끝날 때까지 현재 화면을 유지합니다.
    }

    LaunchedEffect(travelId, spotId, viewModel) {
        viewModel.loadVerification(
            travelId = travelId,
            spotId = spotId,
        )
    }

    /*
     * D00을 별도 화면으로 만들지 않고, 위치 권한이 없을 때
     * 최초 한 번만 Android 시스템 권한창을 표시합니다.
     */
    LaunchedEffect(
        uiState.locationResult,
        hasRequestedLocationPermission,
    ) {
        val needsPermission =
            uiState.locationResult == CurrentLocationResult.PermissionRequired

        if (needsPermission && !hasRequestedLocationPermission) {
            requestLocationPermission()
        }
    }

    /*
     * D02b를 2초간 표시합니다.
     * 전체 인증이면 스티커를 조회하고, 일부 인증이면 일정 화면으로 복귀합니다.
     */
    LaunchedEffect(
        uiState.phase,
        uiState.travel?.certifiedSpotsCount,
        uiState.travel?.certificationTargetCount,
    ) {
        if (uiState.phase != TravelVerificationPhase.VERIFIED) {
            return@LaunchedEffect
        }

        delay(VERIFICATION_COMPLETION_DISPLAY_MILLIS)

        val travel = uiState.travel ?: return@LaunchedEffect
        val isAllCertified = travel.certificationTargetCount > 0 &&
            travel.certifiedSpotsCount == travel.certificationTargetCount

        if (isAllCertified) {
            viewModel.loadStickerReward()
        } else {
            navigator.navigateBack()
        }
    }

    when (uiState.phase) {
        TravelVerificationPhase.VERIFIED,
        TravelVerificationPhase.LOADING_STICKERS -> {
            TravelVerificationCompletedScreen(
                uiState = uiState,
                modifier = modifier,
            )
        }

        TravelVerificationPhase.REWARD -> {
            TravelStickerRewardScreen(
                uiState = uiState,
                onDecoratePhotoClick = onDecoratePhotoClick,
                onLaterClick = {
                    navigator.navigateToTopLevel(HomeNavKey)
                },
                modifier = modifier,
            )
        }

        TravelVerificationPhase.STICKER_NOT_GRANTED -> {
            TravelVerificationScreen(
                uiState = uiState.copy(
                    phase = TravelVerificationPhase.ERROR,
                    errorMessage = "획득한 스티커를 확인하지 못했습니다. 다시 시도해주세요.",
                ),
                onBackClick = navigator::navigateBack,
                onVerifyClick = viewModel::verifySpot,
                onRetryClick = viewModel::retry,
                onRequestLocationPermission = ::requestLocationPermission,
                onOpenLocationSettings = ::openLocationSettings,
                modifier = modifier,
            )
        }

        else -> {
            TravelVerificationScreen(
                uiState = uiState,
                onBackClick = {
                    if (!isBackBlocked) {
                        navigator.navigateBack()
                    }
                },
                onVerifyClick = viewModel::verifySpot,
                onRetryClick = viewModel::retry,
                onRequestLocationPermission = ::requestLocationPermission,
                onOpenLocationSettings = ::openLocationSettings,
                modifier = modifier,
            )
        }
    }
}

/** Compose의 Context를 감싸고 있는 Activity를 찾아 반환합니다. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val VERIFICATION_COMPLETION_DISPLAY_MILLIS = 2_000L

package com.manruhomerun.yadanbeopseok.travel.spot.navigation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.travel.spot.screen.TravelSpotDetailScreen
import com.manruhomerun.yadanbeopseok.travel.spot.viewmodel.TravelSpotDetailViewModel
import com.manruhomerun.yadanbeopseok.ui.tryOpenExternalActivity
import kotlinx.coroutines.launch

/**
 * 관광지 상세 화면과 ViewModel을 연결합니다.
 *
 * 관광지 상세 조회, 이미지 목록 재시도, 찜 변경, 주소 복사, 연락처와 뒤로가기를 연결합니다.
 * 세션 만료에 따른 로그인 화면 전환은 앱의 공통 세션 관찰이 처리합니다.
 */
@Composable
fun TravelSpotDetailRoute(
    travelSpotId: String,
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: TravelSpotDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val telephoneUri = remember(uiState.detail?.telephone) {
        uiState.detail?.telephone?.toDialUriOrNull()
    }

    LaunchedEffect(travelSpotId, viewModel) {
        viewModel.loadTravelSpot(travelSpotId)
    }

    LaunchedEffect(uiState.errorMessage, uiState.hasDetail) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect

        if (!uiState.hasDetail) {
            return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(message = errorMessage)
        viewModel.clearErrorMessage()
    }

    val onBackClick: () -> Unit = {
        if (viewModel.uiState.value.isUpdatingDibs) {
            coroutineScope.launch {
                if (snackbarHostState.currentSnackbarData == null) {
                    snackbarHostState.showSnackbar("찜 상태를 변경하고 있습니다. 잠시만 기다려주세요.")
                }
            }
        } else {
            navigator.navigateBack()
        }
    }
    BackHandler(enabled = uiState.isUpdatingDibs, onBack = onBackClick)

    Box(modifier = modifier.fillMaxSize()) {
        TravelSpotDetailScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onDibsClick = viewModel::toggleDibs,
            onRetryClick = viewModel::retry,
            onImagesRetryClick = viewModel::retryImages,
            onHomepageClick = { homepage ->
                if (!tryOpenExternalActivity { uriHandler.openUri(homepage) }) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("홈페이지를 열 수 없습니다.")
                    }
                }
            },
            onAddressCopyClick = { address ->
                val wasCopied = copyTravelSpotAddress(context, address)
                if (!wasCopied || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    coroutineScope.launch {
                        if (snackbarHostState.currentSnackbarData == null) {
                            snackbarHostState.showSnackbar(
                                message = if (wasCopied) "주소를 복사했어요" else "주소를 복사하지 못했습니다.",
                            )
                        }
                    }
                }
            },
            onTelephoneClick = telephoneUri?.let { uri ->
                {
                    if (!tryOpenExternalActivity { context.startActivity(Intent(Intent.ACTION_DIAL, uri)) }) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("전화 앱을 열 수 없습니다.")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
        )
    }
}

/** 주소만 클립보드에 저장하며, 서비스 사용 불가나 복사 오류는 실패로 반환합니다. */
private fun copyTravelSpotAddress(context: Context, address: String): Boolean {
    if (address.isBlank()) return false

    return try {
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return false
        clipboard.setPrimaryClip(ClipData.newPlainText("관광지 주소", address))
        true
    } catch (_: RuntimeException) {
        false
    }
}

/** 설명문이나 여러 연락처를 임의로 합치지 않고 단일 전화번호만 실행합니다. */
private fun String.toDialUriOrNull(): Uri? {
    val value = trim()
    val singleNumberPattern = Regex(
        """\+?[0-9]{3,15}|[0-9]{4}[- .][0-9]{4}|""" +
            """(?:\+?[0-9]{1,3}[- .])?(?:\([0-9]{2,4}\)|[0-9]{2,4})[- .]?[0-9]{3,4}[- .]?[0-9]{4}""",
    )
    if (!singleNumberPattern.matches(value)) return null
    if (value.count { it in '0'..'9' } !in 3..15) return null

    return Uri.fromParts("tel", value, null)
}

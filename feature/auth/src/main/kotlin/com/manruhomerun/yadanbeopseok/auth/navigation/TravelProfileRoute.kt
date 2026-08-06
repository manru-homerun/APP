package com.manruhomerun.yadanbeopseok.auth.navigation

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.auth.screen.TravelProfileScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.OnboardingViewModel
import com.manruhomerun.yadanbeopseok.navigation.LocalSharedViewModelStoreOwner
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.HomeNavKey

/**
 * 여행 프로필 화면과 온보딩 공유 ViewModel을 연결합니다.
 *
 * 입력 완료 시 온보딩 정보를 서버에 저장하고,
 * 저장 성공 이벤트를 받은 뒤 홈 화면으로 이동합니다.
 */
@Composable
fun TravelProfileRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(viewModelStoreOwner = LocalSharedViewModelStoreOwner.current,),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * 서버에서 온보딩 완료가 확인된 경우에만
     * 인증 화면 백스택을 제거하고 홈으로 이동합니다.
     */
    LaunchedEffect(viewModel, navigator) {
        viewModel.completionEvents.collect {
            navigator.resetTo(HomeNavKey)
        }
    }

    /**
     * 온보딩 저장 오류를 한 번 표시한 뒤
     * ViewModel에 남아 있는 오류 상태를 제거합니다.
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
        TravelProfileScreen(
            residenceRegion = uiState.residenceRegion,
            travelStyleScore = uiState.travelStyleScore,
            preferredTravelRegions = uiState.preferredTravelRegions,
            isStartEnabled = uiState.isOnboardingReadyToSubmit,
            isSubmitting = uiState.isSubmitting,
            onResidenceRegionSelected = viewModel::selectResidenceRegion,
            onTravelStyleScoreChange = viewModel::updateTravelStyleScore,
            onPreferredTravelRegionToggle = viewModel::togglePreferredTravelRegion,
            onBackClick = navigator::navigateBack,
            onStartClick = viewModel::submitOnboarding,
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

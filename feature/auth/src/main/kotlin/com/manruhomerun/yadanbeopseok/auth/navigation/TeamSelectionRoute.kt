package com.manruhomerun.yadanbeopseok.auth.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.auth.screen.TeamSelectionScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.OnboardingViewModel
import com.manruhomerun.yadanbeopseok.navigation.LocalSharedViewModelStoreOwner
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.TravelProfileNavKey

/**
 * 응원 구단 선택 화면과 온보딩 공유 ViewModel, 내비게이션을 연결합니다.
 *
 * 부모 약관 화면이 소유한 [OnboardingViewModel]에
 * 사용자가 선택한 응원 구단을 저장합니다.
 */
@Composable
fun TeamSelectionRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel =
        hiltViewModel(
            viewModelStoreOwner =
                LocalSharedViewModelStoreOwner.current,
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamSelectionScreen(
        selectedTeam = uiState.selectedTeam,
        onTeamSelected = viewModel::selectTeam,
        onBackClick = navigator::navigateBack,
        onNextClick = {
            /*
             * 화면에서 버튼을 비활성화하지만,
             * 이동 직전에도 구단 선택 여부를 확인합니다.
             */
            if (uiState.isTeamSelectionNextEnabled) {
                navigator.navigate(TravelProfileNavKey)
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

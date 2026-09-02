package com.manruhomerun.yadanbeopseok.auth.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.auth.screen.BasicInfoScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.NicknameInputState
import com.manruhomerun.yadanbeopseok.auth.viewmodel.OnboardingViewModel
import com.manruhomerun.yadanbeopseok.navigation.LocalSharedViewModelStoreOwner
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.LoginNavKey
import com.manruhomerun.yadanbeopseok.navigation.route.TeamSelectionNavKey

/**
 * 기본 정보 화면과 온보딩 공유 ViewModel, 내비게이션을 연결합니다.
 */
@Composable
fun BasicInfoRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel =
        hiltViewModel(
            viewModelStoreOwner =
                LocalSharedViewModelStoreOwner.current,
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, navigator) {
        viewModel.sessionExpiredEvents.collect {
            navigator.resetTo(LoginNavKey)
        }
    }

    BasicInfoScreen(
        nickname = uiState.nickname,
        nicknameLength = uiState.nicknameLength,
        isNicknameChecking = uiState.isNicknameChecking,
        isNicknameValid = uiState.isNicknameAvailable,
        hasNicknameValidationError = uiState.hasNicknameValidationError,
        isNicknameCheckRetryEnabled =
            uiState.nicknameInputState == NicknameInputState.CHECK_FAILED,
        nicknameValidationMessage = uiState.nicknameValidationMessage,
        selectedGender = uiState.gender,
        birthDate = uiState.birthDate,
        birthDateValidationMessage = uiState.birthDateValidationMessage,
        isNextEnabled = uiState.isBasicInfoNextEnabled,
        onNicknameChange = viewModel::updateNickname,
        onNicknameCheckRetry = viewModel::retryNicknameAvailabilityCheck,
        onGenderSelect = viewModel::selectGender,
        onBirthDateChange = viewModel::updateBirthDate,
        onBackClick = navigator::navigateBack,
        onNextClick = {
            if (uiState.isBasicInfoNextEnabled) {
                navigator.navigate(TeamSelectionNavKey)
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

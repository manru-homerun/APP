package com.manruhomerun.yadanbeopseok.auth.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.auth.screen.BasicInfoScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.NicknameInputState
import com.manruhomerun.yadanbeopseok.auth.viewmodel.OnboardingViewModel
import com.manruhomerun.yadanbeopseok.navigation.LocalSharedViewModelStoreOwner
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.TeamSelectionNavKey

/**
 * 기본 정보 화면과 온보딩 공유 ViewModel, 내비게이션을 연결합니다.
 *
 * 부모 약관 화면이 소유한 [OnboardingViewModel]에서
 * 닉네임, 성별 및 생년월일 상태를 읽고 변경합니다.
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

    BasicInfoScreen(
        nickname = uiState.nickname,
        isNicknameValid = uiState.nicknameInputState == NicknameInputState.VALID,
        nicknameValidationMessage = uiState.nicknameValidationMessage,
        selectedGender = uiState.gender,
        birthDate = uiState.birthDate,
        birthDateValidationMessage = uiState.birthDateValidationMessage,
        isNextEnabled = uiState.isBasicInfoNextEnabled,
        onNicknameChange = viewModel::updateNickname,
        onGenderSelect = viewModel::selectGender,
        onBirthDateChange = { birthDate -> viewModel.updateBirthDate(birthDate) },
        onBackClick = navigator::navigateBack,
        onNextClick = {
            /*
             * 화면에서도 버튼을 비활성화하지만,
             * 이동 직전에 모든 기본 정보가 유효한지 다시 확인합니다.
             */
            if (uiState.isBasicInfoNextEnabled) {
                navigator.navigate(TeamSelectionNavKey)
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

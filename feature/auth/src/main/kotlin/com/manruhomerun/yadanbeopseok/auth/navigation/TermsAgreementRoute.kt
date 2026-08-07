package com.manruhomerun.yadanbeopseok.auth.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.auth.screen.TermsAgreementScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.OnboardingViewModel
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.BasicInfoNavKey

/**
 * 약관 동의 화면과 온보딩 공유 ViewModel, 내비게이션을 연결합니다.
 *
 * 이 화면의 NavEntry가 [OnboardingViewModel]을 소유하며,
 * 이후 온보딩 화면은 같은 ViewModelStore를 통해 이 인스턴스를 공유합니다.
 */
@Composable
fun TermsAgreementRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    TermsAgreementScreen(
        isServiceTermsAgreed = uiState.isServiceTermsAgreed,
        isPrivacyAgreementAgreed = uiState.isPrivacyAgreementAgreed,
        onServiceTermsAgreementChange = viewModel::updateServiceTermsAgreement,
        onPrivacyAgreementChange = viewModel::updatePrivacyAgreement,
        onAllAgreementChange = viewModel::updateAllAgreements,
        onBackClick = navigator::navigateBack,
        onContinueClick = {
            /*
             * 버튼 비활성화와 별개로 이동 직전에
             * 필수 약관 동의 여부를 다시 확인합니다.
             */
            if (uiState.isAllAgreed) {
                navigator.navigate(BasicInfoNavKey)
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

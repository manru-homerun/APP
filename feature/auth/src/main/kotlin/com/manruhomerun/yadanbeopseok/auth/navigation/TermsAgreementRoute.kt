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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manruhomerun.yadanbeopseok.auth.screen.TermsAgreementScreen
import com.manruhomerun.yadanbeopseok.auth.viewmodel.OnboardingViewModel
import com.manruhomerun.yadanbeopseok.common.LegalDocumentUrl
import com.manruhomerun.yadanbeopseok.navigation.Navigator
import com.manruhomerun.yadanbeopseok.navigation.route.BasicInfoNavKey
import com.manruhomerun.yadanbeopseok.ui.LEGAL_DOCUMENT_OPEN_ERROR_MESSAGE
import com.manruhomerun.yadanbeopseok.ui.tryOpenExternalActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var documentErrorJob by remember(viewModel) { mutableStateOf<Job?>(null) }

    val onOpenDocument: (String) -> Unit = { url ->
        if (tryOpenExternalActivity { uriHandler.openUri(url) }) {
            documentErrorJob?.cancel()
            documentErrorJob = null
        } else if (documentErrorJob?.isActive != true) {
            documentErrorJob = coroutineScope.launch {
                snackbarHostState.showSnackbar(LEGAL_DOCUMENT_OPEN_ERROR_MESSAGE)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        TermsAgreementScreen(
            isServiceTermsAgreed = uiState.isServiceTermsAgreed,
            isPrivacyAgreementAgreed = uiState.isPrivacyAgreementAgreed,
            onServiceTermsAgreementChange = viewModel::updateServiceTermsAgreement,
            onPrivacyAgreementChange = viewModel::updatePrivacyAgreement,
            onAllAgreementChange = viewModel::updateAllAgreements,
            onServiceTermsDetailClick = {
                onOpenDocument(LegalDocumentUrl.TERMS_OF_SERVICE)
            },
            onPrivacyPolicyDetailClick = {
                onOpenDocument(LegalDocumentUrl.PRIVACY_POLICY)
            },
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
            modifier = Modifier.fillMaxSize(),
        )

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

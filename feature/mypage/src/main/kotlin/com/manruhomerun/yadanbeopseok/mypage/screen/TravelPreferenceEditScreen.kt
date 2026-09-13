 package com.manruhomerun.yadanbeopseok.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import com.manruhomerun.yadanbeopseok.mypage.viewmodel.TravelPreferenceEditUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPreferenceForm

/** H·03 여행 취향 수정 화면입니다. */
@Composable
fun TravelPreferenceEditScreen(
    uiState: TravelPreferenceEditUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onResidenceRegionSelected: (ProfileRegion) -> Unit,
    onTravelStyleScoreChange: (TravelStyleScore) -> Unit,
    onPreferredTravelRegionToggle: (ProfileRegion) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanTopAppBar(
            title = "취향 수정",
            onNavigationClick = {
                if (!uiState.isSaving) {
                    onBackClick()
                }
            },
            modifier = Modifier.statusBarsPadding(),
        )

        when {
            uiState.isLoading -> {
                TravelPreferenceEditLoadingContent(
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.originalPreference == null -> {
                TravelPreferenceEditErrorContent(
                    message = uiState.errorMessage
                        ?: "여행 취향을 불러오지 못했습니다.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                TravelPreferenceEditContent(
                    uiState = uiState,
                    onResidenceRegionSelected = onResidenceRegionSelected,
                    onTravelStyleScoreChange = onTravelStyleScoreChange,
                    onPreferredTravelRegionToggle = onPreferredTravelRegionToggle,
                    onSaveClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 여행 취향을 처음 불러오는 동안 진행 상태를 표시합니다. */
@Composable
private fun TravelPreferenceEditLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/** 여행 취향 조회 실패 문구와 재시도 버튼을 표시합니다. */
@Composable
private fun TravelPreferenceEditErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "여행 취향을 확인할 수 없습니다",
            style = YadanTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )

        YadanButton(
            text = "다시 시도",
            onClick = onRetryClick,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
        )
    }
}

/** 편집 가능한 여행 취향 입력 항목과 저장 버튼을 표시합니다. */
@Composable
private fun TravelPreferenceEditContent(
    uiState: TravelPreferenceEditUiState,
    onResidenceRegionSelected: (ProfileRegion) -> Unit,
    onTravelStyleScoreChange: (TravelStyleScore) -> Unit,
    onPreferredTravelRegionToggle: (ProfileRegion) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                top = 2.dp,
                end = 20.dp,
                bottom = 16.dp,
            )
            .navigationBarsPadding(),
    ) {
        YadanTravelPreferenceForm(
            residenceRegion = uiState.residenceRegion,
            travelStyleScore = uiState.travelStyleScore,
            preferredTravelRegions = uiState.preferredTravelRegions,
            onResidenceRegionSelected = onResidenceRegionSelected,
            onTravelStyleScoreChange = onTravelStyleScoreChange,
            onPreferredTravelRegionToggle = onPreferredTravelRegionToggle,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
        )

        Spacer(modifier = Modifier.height(18.dp))

        YadanButton(
            text = "저장",
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isSaveEnabled,
            isLoading = uiState.isSaving,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
    }
}

private val previewTravelPreference = TravelPreference(
    residenceRegion = ProfileRegion.BUSAN,
    travelStyleScore = TravelStyleScore(3),
    preferredTravelRegions = listOf(ProfileRegion.BUSAN, ProfileRegion.JEJU),
)

@Preview(name = "H03 취향 수정", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelPreferenceEditScreenPreview() {
    YadanbeopseokTheme {
        TravelPreferenceEditScreen(
            uiState = TravelPreferenceEditUiState(
                originalPreference = previewTravelPreference,
                residenceRegion = previewTravelPreference.residenceRegion,
                travelStyleScore = previewTravelPreference.travelStyleScore,
                preferredTravelRegions = previewTravelPreference.preferredTravelRegions,
                isLoading = false,
            ),
            onBackClick = {},
            onRetryClick = {},
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H03 취향 저장 중", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelPreferenceEditSavingPreview() {
    YadanbeopseokTheme {
        TravelPreferenceEditScreen(
            uiState = TravelPreferenceEditUiState(
                originalPreference = previewTravelPreference,
                residenceRegion = previewTravelPreference.residenceRegion,
                travelStyleScore = previewTravelPreference.travelStyleScore,
                preferredTravelRegions = previewTravelPreference.preferredTravelRegions,
                isLoading = false,
                isSaving = true,
            ),
            onBackClick = {},
            onRetryClick = {},
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H03 취향 불러오는 중", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelPreferenceEditLoadingPreview() {
    YadanbeopseokTheme {
        TravelPreferenceEditScreen(
            uiState = TravelPreferenceEditUiState(),
            onBackClick = {},
            onRetryClick = {},
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            onSaveClick = {},
        )
    }
}

@Preview(name = "H03 취향 조회 오류", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelPreferenceEditErrorPreview() {
    YadanbeopseokTheme {
        TravelPreferenceEditScreen(
            uiState = TravelPreferenceEditUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onBackClick = {},
            onRetryClick = {},
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            onSaveClick = {},
        )
    }
}

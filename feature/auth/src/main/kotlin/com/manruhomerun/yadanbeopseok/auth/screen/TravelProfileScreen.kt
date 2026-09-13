package com.manruhomerun.yadanbeopseok.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelPreferenceForm

/**
 * 신규 회원이 거주 지역과 여행 취향을 입력하는 화면입니다.
 *
 * 지역 및 여행 성향 입력에는 기존 core/ui 컴포넌트를 재사용합니다.
 */
@Composable
fun TravelProfileScreen(
    residenceRegion: ProfileRegion?,
    travelStyleScore: TravelStyleScore,
    preferredTravelRegions: List<ProfileRegion>,
    isStartEnabled: Boolean,
    isSubmitting: Boolean,
    onResidenceRegionSelected: (ProfileRegion) -> Unit,
    onTravelStyleScoreChange: (TravelStyleScore) -> Unit,
    onPreferredTravelRegionToggle: (ProfileRegion) -> Unit,
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(YadanBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "여행 프로필",
            onNavigationClick = {
                if (!isSubmitting) {
                    onBackClick()
                }
            },
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 22.dp,
                    ),
        ) {
            Text(
                text = "여행 취향을 알려주세요",
                style =
                    YadanTypography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "맞춤 코스 추천에 활용해요.",
                style = YadanTypography.bodyMedium,
                color = YadanTextSecondary,
            )

            Spacer(modifier = Modifier.height(22.dp))

            YadanTravelPreferenceForm(
                residenceRegion = residenceRegion,
                travelStyleScore = travelStyleScore,
                preferredTravelRegions = preferredTravelRegions,
                onResidenceRegionSelected = onResidenceRegionSelected,
                onTravelStyleScoreChange = onTravelStyleScoreChange,
                onPreferredTravelRegionToggle = onPreferredTravelRegionToggle,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )

            Spacer(modifier = Modifier.height(18.dp))

            YadanButton(
                text = "시작하기",
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isStartEnabled,
                isLoading = isSubmitting,
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
}

@Preview(
    name = "Travel profile",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelProfileScreenPreview() {
    YadanbeopseokTheme {
        TravelProfileScreen(
            residenceRegion = ProfileRegion.BUSAN,
            travelStyleScore = TravelStyleScore(value = 3),
            preferredTravelRegions =
                listOf(
                    ProfileRegion.BUSAN,
                    ProfileRegion.JEJU,
                ),
            isStartEnabled = true,
            isSubmitting = false,
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            onBackClick = {},
            onStartClick = {},
        )
    }
}

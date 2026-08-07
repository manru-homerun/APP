package com.manruhomerun.yadanbeopseok.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionMetaText
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import com.manruhomerun.yadanbeopseok.ui.component.YadanPreferredTravelRegionItem
import com.manruhomerun.yadanbeopseok.ui.component.YadanResidenceRegionSelector
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelStyleSlider

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

            YadanSectionHeader(
                title = "거주 지역",
            )

            Spacer(modifier = Modifier.height(9.dp))

            YadanResidenceRegionSelector(
                selectedRegion = residenceRegion,
                onRegionSelected = onResidenceRegionSelected,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )

            Spacer(modifier = Modifier.height(22.dp))

            YadanSectionHeader(
                title = "여행 스타일",
            )

            Spacer(modifier = Modifier.height(9.dp))

            YadanTravelStyleSlider(
                score = travelStyleScore,
                onScoreChange = onTravelStyleScoreChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
            )

            Spacer(modifier = Modifier.height(22.dp))

            YadanSectionHeader(
                title = "선호 여행 지역",
                trailingContent = {
                    YadanSectionMetaText(
                        text = "복수 선택 가능",
                    )
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            PreferredTravelRegionGrid(
                selectedRegions = preferredTravelRegions,
                onRegionToggle = onPreferredTravelRegionToggle,
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

/**
 * 선호 여행 지역을 4열 그리드로 표시합니다.
 *
 * 16개 지역을 4열 4행으로 동일한 너비로 배치합니다.
 */
@Composable
private fun PreferredTravelRegionGrid(
    selectedRegions: List<ProfileRegion>,
    onRegionToggle: (ProfileRegion) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(REGION_GRID_SPACING),
    ) {
        ProfileRegion.preferredTravelOptions
            .chunked(REGION_COLUMN_COUNT)
            .forEach { regions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(REGION_GRID_SPACING),
                ) {
                    regions.forEach { region ->
                        YadanPreferredTravelRegionItem(
                            region = region,
                            selected = region in selectedRegions,
                            onClick = {
                                onRegionToggle(region)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = enabled,
                        )
                    }

                    repeat(REGION_COLUMN_COUNT - regions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
    }
}

private const val REGION_COLUMN_COUNT = 4
private val REGION_GRID_SPACING = 7.dp

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

package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.MAX_PREFERRED_TRAVEL_REGION_COUNT
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore

/**
 * 거주 지역, 여행 스타일과 선호 여행 지역을 입력하는 공통 폼입니다.
 *
 * G·05 온보딩과 H·03 취향 수정 화면에서 동일한 입력 영역을 재사용합니다.
 * 화면 제목, 스크롤, 저장 버튼과 로딩 상태는 각 화면에서 처리합니다.
 */
@Composable
fun YadanTravelPreferenceForm(
    residenceRegion: ProfileRegion?,
    travelStyleScore: TravelStyleScore,
    preferredTravelRegions: List<ProfileRegion>,
    onResidenceRegionSelected: (ProfileRegion) -> Unit,
    onTravelStyleScoreChange: (TravelStyleScore) -> Unit,
    onPreferredTravelRegionToggle: (ProfileRegion) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        YadanSectionHeader(title = "거주 지역")

        Spacer(modifier = Modifier.height(9.dp))

        YadanResidenceRegionSelector(
            selectedRegion = residenceRegion,
            onRegionSelected = onResidenceRegionSelected,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(22.dp))

        YadanSectionHeader(title = "여행 스타일")

        Spacer(modifier = Modifier.height(9.dp))

        YadanTravelStyleSlider(
            score = travelStyleScore,
            onScoreChange = onTravelStyleScoreChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )

        Spacer(modifier = Modifier.height(22.dp))

        YadanSectionHeader(title = "선호 여행 지역")

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "최소 1개, 최대 ${MAX_PREFERRED_TRAVEL_REGION_COUNT}개 선택해주세요.",
            modifier = Modifier.fillMaxWidth(),
            style = YadanTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = YadanTextMuted,
        )

        Spacer(modifier = Modifier.height(10.dp))

        PreferredTravelRegionGrid(
            selectedRegions = preferredTravelRegions,
            onRegionToggle = onPreferredTravelRegionToggle,
            enabled = enabled,
        )
    }
}

/** 16개 선호 여행 지역을 4열 그리드로 표시합니다. */
@Composable
private fun PreferredTravelRegionGrid(
    selectedRegions: List<ProfileRegion>,
    onRegionToggle: (ProfileRegion) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(REGION_GRID_SPACING),
    ) {
        ProfileRegion.preferredTravelOptions
            .chunked(REGION_COLUMN_COUNT)
            .forEach { regions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(REGION_GRID_SPACING),
                ) {
                    regions.forEach { region ->
                        val isSelected = region in selectedRegions
                        val isRegionEnabled = enabled &&
                            (isSelected || selectedRegions.size < MAX_PREFERRED_TRAVEL_REGION_COUNT)

                        YadanPreferredTravelRegionItem(
                            region = region,
                            selected = isSelected,
                            onClick = {
                                onRegionToggle(region)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = isRegionEnabled,
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

@Preview(name = "여행 취향 입력 폼", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "여행 취향 입력 폼 - 좁은 화면", showBackground = true, widthDp = 320, heightDp = 640)
@Preview(name = "여행 취향 입력 폼 - 큰 글꼴", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Preview(name = "여행 취향 입력 폼 - 최대 글꼴", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun YadanTravelPreferenceFormPreview(
    @PreviewParameter(TravelPreferenceFormPreviewProvider::class) state: TravelPreferenceFormPreviewState,
) {
    YadanbeopseokTheme {
        YadanTravelPreferenceForm(
            residenceRegion = ProfileRegion.BUSAN,
            travelStyleScore = TravelStyleScore(3),
            preferredTravelRegions = state.selectedRegions,
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            modifier = Modifier
                .background(YadanBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            enabled = state.enabled,
        )
    }
}

private data class TravelPreferenceFormPreviewState(
    val selectedRegions: List<ProfileRegion>,
    val enabled: Boolean = true,
)

private class TravelPreferenceFormPreviewProvider : PreviewParameterProvider<TravelPreferenceFormPreviewState> {
    private val regions = ProfileRegion.preferredTravelOptions.take(MAX_PREFERRED_TRAVEL_REGION_COUNT)

    override val values = sequenceOf(
        TravelPreferenceFormPreviewState(listOf(ProfileRegion.BUSAN, ProfileRegion.JEJU)),
        TravelPreferenceFormPreviewState(emptyList()),
        TravelPreferenceFormPreviewState(regions.take(1)),
        TravelPreferenceFormPreviewState(regions),
        TravelPreferenceFormPreviewState(regions, enabled = false),
    )
}

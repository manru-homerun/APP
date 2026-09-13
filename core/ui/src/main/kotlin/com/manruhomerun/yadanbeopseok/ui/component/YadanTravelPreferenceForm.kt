package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionMetaText
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
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

        YadanSectionHeader(
            title = "선호 여행 지역",
            trailingContent = {
                YadanSectionMetaText(text = "복수 선택 가능")
            },
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

@Preview(name = "여행 취향 입력 폼", showBackground = true, widthDp = 390)
@Composable
private fun YadanTravelPreferenceFormPreview() {
    YadanbeopseokTheme {
        YadanTravelPreferenceForm(
            residenceRegion = ProfileRegion.BUSAN,
            travelStyleScore = TravelStyleScore(3),
            preferredTravelRegions = listOf(ProfileRegion.BUSAN, ProfileRegion.JEJU),
            onResidenceRegionSelected = {},
            onTravelStyleScoreChange = {},
            onPreferredTravelRegionToggle = {},
            modifier = Modifier
                .background(YadanBackground)
                .padding(20.dp),
        )
    }
}

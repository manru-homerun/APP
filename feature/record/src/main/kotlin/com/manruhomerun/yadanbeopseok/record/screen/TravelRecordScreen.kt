package com.manruhomerun.yadanbeopseok.record.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanSectionMetaText
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryTint
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import com.manruhomerun.yadanbeopseok.record.component.TravelRecordMap
import com.manruhomerun.yadanbeopseok.record.viewmodel.TravelRecordUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanTravelRecordCard
import kotlinx.datetime.LocalDate

/**
 * D·01 여행 기록 메인 화면입니다.
 *
 * 선택한 시즌의 완료 여행을 지역별 지도와 목록으로 표시합니다.
 */
@Composable
fun TravelRecordScreen(
    uiState: TravelRecordUiState,
    onSeasonSelected: (Int) -> Unit,
    onTravelClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorMessage = uiState.errorMessage
    val hasLoadedTravels = uiState.completedTravels.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanMainHeader(
            title = "여행 기록",
            modifier = Modifier.statusBarsPadding(),
        )

        when {
            uiState.isLoading && !hasLoadedTravels -> {
                TravelRecordLoadingContent(
                    modifier = Modifier.weight(1f),
                )
            }

            errorMessage != null && !hasLoadedTravels -> {
                TravelRecordErrorContent(
                    message = errorMessage,
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.isEmpty -> {
                TravelRecordEmptyContent(
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                TravelRecordLoadedContent(
                    uiState = uiState,
                    onSeasonSelected = onSeasonSelected,
                    onTravelClick = onTravelClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * 완료 여행이 존재할 때 시즌, 지도와 여행 목록을 표시합니다.
 */
@Composable
private fun TravelRecordLoadedContent(
    uiState: TravelRecordUiState,
    onSeasonSelected: (Int) -> Unit,
    onTravelClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMapUnavailable by rememberSaveable(uiState.selectedSeason) {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 24.dp,
        ),
    ) {
        item(key = "season-selector") {
            val selectedSeason = uiState.selectedSeason

            if (selectedSeason != null) {
                TravelSeasonSelector(
                    selectedSeason = selectedSeason,
                    availableSeasons = uiState.availableSeasons,
                    onSeasonSelected = onSeasonSelected,
                )
            }
        }

        item(key = "travel-record-map") {
            if (isMapUnavailable) {
                TravelRecordMapErrorContent(
                    onRetryClick = {
                        isMapUnavailable = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
            } else {
                TravelRecordMap(
                    regionVisitCounts = uiState.regionVisitCounts,
                    onError = {
                        isMapUnavailable = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(TRAVEL_RECORD_MAP_HEIGHT)
                        .clip(YadanShapes.medium)
                        .border(
                            width = 1.dp,
                            color = YadanOutline,
                            shape = YadanShapes.medium,
                        ),
                )
            }
        }

        item(key = "map-caption") {
            TravelRecordMapCaption(
                modifier = Modifier.padding(
                    start = 2.dp,
                    top = 9.dp,
                ),
            )
        }

        item(key = "completed-travel-header") {
            YadanSectionHeader(
                title = "종료된 여행",
                modifier = Modifier.padding(
                    start = 2.dp,
                    top = 20.dp,
                    end = 2.dp,
                    bottom = 10.dp,
                ),
                trailingContent = {
                    YadanSectionMetaText(
                        text = "${uiState.completedTravelCount}건",
                    )
                },
            )
        }

        items(
            items = uiState.visibleTravels,
            key = { travel -> travel.id },
        ) { travel ->
            YadanTravelRecordCard(
                travel = travel,
                onClick = {
                    onTravelClick(travel.id)
                },
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
    }
}

/**
 * 조회된 완료 여행 중 화면에 표시할 시즌을 선택합니다.
 */
@Composable
private fun TravelSeasonSelector(
    selectedSeason: Int,
    availableSeasons: List<Int>,
    onSeasonSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(modifier = modifier) {
        Surface(
            onClick = {
                expanded = true
            },
            modifier = Modifier.widthIn(min = 116.dp),
            shape = YadanShapes.small,
            color = YadanSurface,
            border = BorderStroke(
                width = 1.dp,
                color = YadanOutline,
            ),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 13.dp,
                    top = 7.dp,
                    end = 9.dp,
                    bottom = 7.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$selectedSeason 시즌",
                    style = YadanTypography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanTextPrimary,
                    maxLines = 1,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "시즌 선택",
                    modifier = Modifier.size(18.dp),
                    tint = YadanTextMuted,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            availableSeasons.forEach { season ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$season 시즌",
                            style = YadanTypography.bodyMedium,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSeasonSelected(season)
                    },
                    trailingIcon = {
                        if (season == selectedSeason) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = YadanPrimary,
                            )
                        }
                    },
                )
            }
        }
    }
}

/**
 * 지도 원의 의미를 설명하는 안내 문구입니다.
 */
@Composable
private fun TravelRecordMapCaption(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = YadanPrimary.copy(alpha = 0.16f),
                    shape = CircleShape,
                )
                .border(
                    width = 1.5.dp,
                    color = YadanPrimary,
                    shape = CircleShape,
                ),
        )

        Text(
            text = "원 크기 = 도시별 방문 횟수",
            style = YadanTypography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = YadanTextMuted,
        )
    }
}

/**
 * 카카오맵을 표시하지 못했을 때 지도 영역 안에서 재시도를 제공합니다.
 */
@Composable
private fun TravelRecordMapErrorContent(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(TRAVEL_RECORD_MAP_HEIGHT),
        shape = YadanShapes.medium,
        color = YadanPrimaryTint,
        border = BorderStroke(
            width = 1.dp,
            color = YadanOutline,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "지도를 불러오지 못했습니다",
                style = YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = YadanTextPrimary,
            )

            TextButton(onClick = onRetryClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "다시 시도",
                    style = YadanTypography.labelMedium,
                )
            }
        }
    }
}

/**
 * 완료 여행 목록을 처음 불러오는 동안 표시합니다.
 */
@Composable
private fun TravelRecordLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 완료 여행 목록 조회에 실패했을 때 안내와 재시도를 표시합니다.
 */
@Composable
private fun TravelRecordErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "여행 기록을 확인할 수 없습니다",
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
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
                .widthIn(min = 120.dp),
        )
    }
}

/**
 * 선택할 수 있는 완료 여행이 없을 때 안내를 표시합니다.
 */
@Composable
private fun TravelRecordEmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "아직 종료된 여행이 없습니다",
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "완료한 여행이 생기면 이곳에서 기록을 확인할 수 있습니다.",
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private val TRAVEL_RECORD_MAP_HEIGHT = 240.dp

@Preview(
    name = "D01 여행 기록",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordScreenPreview() {
    YadanbeopseokTheme {
        TravelRecordScreen(
            uiState = TravelRecordUiState(
                completedTravels = previewCompletedTravels,
                selectedSeason = 2026,
                isLoading = false,
            ),
            onSeasonSelected = {},
            onTravelClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "D01 여행 기록 로딩",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordLoadingPreview() {
    YadanbeopseokTheme {
        TravelRecordScreen(
            uiState = TravelRecordUiState(),
            onSeasonSelected = {},
            onTravelClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "D01 여행 기록 오류",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordErrorPreview() {
    YadanbeopseokTheme {
        TravelRecordScreen(
            uiState = TravelRecordUiState(
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onSeasonSelected = {},
            onTravelClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "D01 여행 기록 없음",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelRecordEmptyPreview() {
    YadanbeopseokTheme {
        TravelRecordScreen(
            uiState = TravelRecordUiState(
                isLoading = false,
            ),
            onSeasonSelected = {},
            onTravelClick = {},
            onRetryClick = {},
        )
    }
}

private val previewCompletedTravels = listOf(
    TravelSummary(
        id = "234",
        name = "부산 사직 직관 여행",
        startDate = LocalDate(2026, 4, 18),
        endDate = LocalDate(2026, 4, 19),
        baseballGameId = "123",
        homeTeam = KboTeam.LOTTE,
        awayTeam = KboTeam.KIA,
        region = Region.BUSAN,
        isLeader = true,
        spotsCount = 4,
        certificationTargetCount = 4,
        certifiedSpotsCount = 4,
        hasSticker = true,
    ),
    TravelSummary(
        id = "235",
        name = "대구 라이온즈파크 여행",
        startDate = LocalDate(2026, 4, 4),
        endDate = LocalDate(2026, 4, 5),
        baseballGameId = "124",
        homeTeam = KboTeam.SAMSUNG,
        awayTeam = KboTeam.KIA,
        region = Region.DAEGU,
        isLeader = true,
        spotsCount = 3,
        certificationTargetCount = 3,
        certifiedSpotsCount = 2,
        hasSticker = false,
    ),
)

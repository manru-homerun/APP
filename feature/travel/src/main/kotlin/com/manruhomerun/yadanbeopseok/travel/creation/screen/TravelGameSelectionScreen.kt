package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.BaseballStadiumSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.TravelGameSelectionUiState
import com.manruhomerun.yadanbeopseok.ui.component.YadanBaseballTeamFilter
import com.manruhomerun.yadanbeopseok.ui.component.YadanGameSelectionCard
import kotlinx.datetime.LocalDateTime

/**
 * B·01 여행 만들기의 경기 선택 화면입니다.
 *
 * 선택한 구단의 요청일 기준 최대 2주 경기 일정을 보여주고,
 * 한 경기를 선택해 다음 단계로 이동할 수 있도록 합니다.
 */
@Composable
fun TravelGameSelectionScreen(
    uiState: TravelGameSelectionUiState,
    onCloseClick: () -> Unit,
    onTeamSelected: (KboTeam) -> Unit,
    onGameSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelectionEnabled = !uiState.isGameDetailLoading
    val isNextEnabled = uiState.selectedGame != null && !uiState.isScheduleLoading

    TravelCreationScaffold(
        currentStep = TravelCreationStep.GAME_SELECTION,
        title = "직관할 경기를 골라요",
        description = "앞으로 2주간 응원 구단의 경기를 날짜순으로 보여드려요.",
        onNavigationClick = onCloseClick,
        modifier = modifier,
        bottomBar = {
            YadanButton(
                text = "다음",
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        top = 11.dp,
                        end = 24.dp,
                        bottom = 20.dp,
                    ),
                enabled = isNextEnabled,
                isLoading = uiState.isGameDetailLoading,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
        },
    ) {
        item(key = "team_filter") {
            YadanBaseballTeamFilter(
                selectedTeam = uiState.selectedTeam,
                onTeamSelected = onTeamSelected,
                enabled = isSelectionEnabled,
            )
        }

        when {
            uiState.selectedTeam == null || uiState.isScheduleLoading -> {
                item(key = "loading") {
                    TravelGameSelectionLoadingContent()
                }
            }

            uiState.games.isNotEmpty() -> {
                item(key = "games") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        uiState.games.forEach { game ->
                            key(game.id) {
                                YadanGameSelectionCard(
                                    game = game,
                                    selected = uiState.selectedGameId == game.id,
                                    onClick = {
                                        onGameSelected(game.id)
                                    },
                                    enabled = isSelectionEnabled,
                                )
                            }
                        }
                    }
                }
            }

            uiState.errorMessage != null -> {
                item(key = "error") {
                    TravelGameSelectionErrorContent(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick,
                    )
                }
            }

            else -> {
                item(key = "empty") {
                    TravelGameSelectionEmptyContent(
                        selectedTeam = uiState.selectedTeam,
                    )
                }
            }
        }
    }
}

/**
 * 경기 일정을 조회하는 동안 진행 상태를 표시합니다.
 */
@Composable
private fun TravelGameSelectionLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 경기 일정 조회 실패 안내와 재시도 버튼을 표시합니다.
 */
@Composable
private fun TravelGameSelectionErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "경기 일정을 확인할 수 없습니다",
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
 * 선택한 구단의 예정된 경기가 없을 때 안내 문구를 표시합니다.
 */
@Composable
private fun TravelGameSelectionEmptyContent(
    selectedTeam: KboTeam,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "${selectedTeam.displayName}의 예정된 경기가 없습니다",
            style = YadanTypography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = YadanTextPrimary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "새로운 경기 일정이 등록되면 이곳에 표시됩니다.",
            modifier = Modifier.padding(top = 8.dp),
            style = YadanTypography.bodySmall,
            color = YadanTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    name = "B01 경기 선택",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelGameSelectionScreenPreview() {
    YadanbeopseokTheme {
        TravelGameSelectionScreen(
            uiState = TravelGameSelectionUiState(
                selectedTeam = KboTeam.KIA,
                games = previewTravelGames,
                selectedGameId = "game-1",
            ),
            onCloseClick = {},
            onTeamSelected = {},
            onGameSelected = {},
            onNextClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "B01 경기 선택 로딩",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelGameSelectionLoadingPreview() {
    YadanbeopseokTheme {
        TravelGameSelectionScreen(
            uiState = TravelGameSelectionUiState(
                selectedTeam = KboTeam.KIA,
                isScheduleLoading = true,
            ),
            onCloseClick = {},
            onTeamSelected = {},
            onGameSelected = {},
            onNextClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "B01 경기 선택 오류",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelGameSelectionErrorPreview() {
    YadanbeopseokTheme {
        TravelGameSelectionScreen(
            uiState = TravelGameSelectionUiState(
                selectedTeam = KboTeam.KIA,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onCloseClick = {},
            onTeamSelected = {},
            onGameSelected = {},
            onNextClick = {},
            onRetryClick = {},
        )
    }
}

private val previewTravelGames = listOf(
    BaseballGameSummary(
        id = "game-1",
        stadium = BaseballStadiumSummary(
            id = "stadium-1",
            name = "광주-KIA 챔피언스필드",
        ),
        homeTeam = KboTeam.KIA,
        awayTeam = KboTeam.LG,
        gameDateTime = LocalDateTime(2026, 5, 23, 17, 0),
    ),
    BaseballGameSummary(
        id = "game-2",
        stadium = BaseballStadiumSummary(
            id = "stadium-2",
            name = "잠실야구장",
        ),
        homeTeam = KboTeam.DOOSAN,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(2026, 5, 24, 14, 0),
    ),
    BaseballGameSummary(
        id = "game-3",
        stadium = BaseballStadiumSummary(
            id = "stadium-3",
            name = "수원 KT 위즈파크",
        ),
        homeTeam = KboTeam.KT,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(2026, 5, 26, 18, 30),
    ),
)

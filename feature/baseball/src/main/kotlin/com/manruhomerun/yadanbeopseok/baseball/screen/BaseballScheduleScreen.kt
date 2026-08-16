package com.manruhomerun.yadanbeopseok.baseball.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.baseball.viewmodel.BaseballScheduleUiState
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeader
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOutline
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.BaseballStadiumSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.ui.component.YadanGameScheduleCard
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate

/**
 * A·05 구단별 경기 일정 화면입니다.
 *
 * 구단은 서버 ID 순서로 표시하며, 선택한 구단의 요청일 기준
 * 최대 2주 경기 일정을 날짜별로 구분하여 보여줍니다.
 */
@Composable
fun BaseballScheduleScreen(
    uiState: BaseballScheduleUiState,
    onTeamSelected: (KboTeam) -> Unit,
    onPlanClick: ((String) -> Unit)?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gamesByDate = uiState.games.groupBy { game -> game.gameDateTime.date }
    val isPlanEnabled = onPlanClick != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground),
    ) {
        YadanMainHeader(
            title = "경기 일정",
            modifier = Modifier.statusBarsPadding(),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 8.dp,
                end = 20.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "team-filter") {
                BaseballTeamFilter(
                    selectedTeam = uiState.selectedTeam,
                    onTeamSelected = onTeamSelected,
                )
            }

            when {
                uiState.isLoading -> {
                    item(key = "loading") {
                        BaseballScheduleLoadingContent()
                    }
                }

                uiState.errorMessage != null -> {
                    item(key = "error") {
                        BaseballScheduleErrorContent(
                            message = uiState.errorMessage,
                            onRetryClick = onRetryClick,
                        )
                    }
                }

                uiState.games.isEmpty() -> {
                    item(key = "empty") {
                        BaseballScheduleEmptyContent(
                            selectedTeam = uiState.selectedTeam,
                        )
                    }
                }

                else -> {
                    gamesByDate.forEach { (date, gamesOnDate) ->
                        item(key = "date-$date") {
                            BaseballGameDateHeader(date = date)
                        }

                        items(
                            items = gamesOnDate,
                            key = { game -> game.id },
                        ) { game ->
                            YadanGameScheduleCard(
                                game = game,
                                onPlanClick = { onPlanClick?.invoke(game.id) },
                                enabled = isPlanEnabled,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 일정 조회에 사용할 KBO 구단 필터를 표시합니다.
 *
 * 서버 구단 ID 순서로 정렬하므로 첫 구단은 KIA입니다.
 */
@Composable
private fun BaseballTeamFilter(
    selectedTeam: KboTeam,
    onTeamSelected: (KboTeam) -> Unit,
    modifier: Modifier = Modifier,
) {
    val teams = KboTeam.entries.sortedBy { team -> team.serverId }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .clip(YadanShapes.medium)
            .background(YadanDivider),
        contentPadding = PaddingValues(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            items = teams,
            key = { team -> team.serverId },
        ) { team ->
            YadanFilterChip(
                text = team.displayName,
                selected = selectedTeam == team,
                onClick = { onTeamSelected(team) },
            )
        }
    }
}

/**
 * 동일한 날짜에 열리는 경기들 위에 날짜를 표시합니다.
 */
@Composable
private fun BaseballGameDateHeader(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = YadanShapes.small,
        color = YadanSurface,
        border = BorderStroke(
            width = 1.dp,
            color = YadanOutline,
        ),
    ) {
        Text(
            text = date.toScheduleDateText(),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp,
            ),
            style = YadanTypography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            color = YadanTextPrimary,
        )
    }
}

/**
 * 경기 일정을 불러오는 동안 진행 상태를 표시합니다.
 */
@Composable
private fun BaseballScheduleLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = YadanPrimary)
    }
}

/**
 * 경기 일정 조회 실패 문구와 재시도 버튼을 표시합니다.
 */
@Composable
private fun BaseballScheduleErrorContent(
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
private fun BaseballScheduleEmptyContent(
    selectedTeam: KboTeam,
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

private val scheduleDateFormatter = DateTimeFormatter.ofPattern(
    "M.d E",
    Locale.KOREAN,
)

/**
 * 경기 날짜를 A·05의 `5.23 토` 형식으로 변환합니다.
 */
private fun LocalDate.toScheduleDateText(): String =
    toJavaLocalDate().format(scheduleDateFormatter)

@Preview(
    name = "경기 일정",
    showBackground = true,
)
@Composable
private fun BaseballScheduleScreenPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                games = previewBaseballGames,
                isLoading = false,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "경기 일정 로딩",
    showBackground = true,
)
@Composable
private fun BaseballScheduleLoadingPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                isLoading = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(
    name = "경기 일정 오류",
    showBackground = true,
)
@Composable
private fun BaseballScheduleErrorPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
        )
    }
}

private val previewBaseballGames = listOf(
    BaseballGameSummary(
        id = "game-1",
        stadium = BaseballStadiumSummary(
            id = "stadium-1",
            name = "광주-KIA 챔피언스필드",
        ),
        homeTeam = KboTeam.KIA,
        awayTeam = KboTeam.LG,
        gameDateTime = LocalDateTime(
            year = 2026,
            month = 5,
            day = 23,
            hour = 17,
            minute = 0,
        ),
    ),
    BaseballGameSummary(
        id = "game-2",
        stadium = BaseballStadiumSummary(
            id = "stadium-2",
            name = "잠실야구장",
        ),
        homeTeam = KboTeam.DOOSAN,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(
            year = 2026,
            month = 5,
            day = 24,
            hour = 14,
            minute = 0,
        ),
    ),
    BaseballGameSummary(
        id = "game-3",
        stadium = BaseballStadiumSummary(
            id = "stadium-3",
            name = "수원 KT 위즈파크",
        ),
        homeTeam = KboTeam.KT,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(
            year = 2026,
            month = 5,
            day = 24,
            hour = 17,
            minute = 0,
        ),
    ),
)

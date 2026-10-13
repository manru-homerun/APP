package com.manruhomerun.yadanbeopseok.baseball.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.baseball.viewmodel.BaseballScheduleUiState
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanMainHeader
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
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
import com.manruhomerun.yadanbeopseok.ui.component.YadanBaseballTeamFilter
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
    onBackClick: (() -> Unit)? = null,
    onTeamSelected: (KboTeam) -> Unit,
    onPlanClick: ((String) -> Unit)?,
    onRetryClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gamesByDate = uiState.games.groupBy { game -> game.gameDateTime.date }
    val isPlanEnabled = onPlanClick != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        if (onBackClick == null) {
            YadanMainHeader(
                title = "경기 일정",
                modifier = Modifier.statusBarsPadding(),
            )
        } else {
            YadanTopAppBar(
                title = "경기 일정",
                onNavigationClick = onBackClick,
                modifier = Modifier.statusBarsPadding(),
            )
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 8.dp,
                    end = 20.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "team-filter") {
                    YadanBaseballTeamFilter(
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

                    !uiState.hasLoadedSchedule && uiState.errorMessage != null -> {
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
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun BaseballScheduleScreenPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                games = previewBaseballGames,
                isLoading = false,
                hasLoadedSchedule = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(name = "경기 일정 - 중간 구단 선택", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BaseballScheduleMiddleTeamPreview() {
    BaseballScheduleTeamFilterPreviewContent(team = KboTeam.KT)
}

@Preview(name = "경기 일정 - 마지막 구단 선택", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "경기 일정 - 마지막 구단과 좁은 화면", showBackground = true, widthDp = 320, heightDp = 640)
@Preview(name = "경기 일정 - 마지막 구단과 큰 글씨", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 2f)
@Composable
private fun BaseballScheduleLastTeamPreview() {
    BaseballScheduleTeamFilterPreviewContent(team = KboTeam.NC)
}

/** 구단 필터를 전체 경기 일정 화면에서 확인하는 Preview입니다. */
@Composable
private fun BaseballScheduleTeamFilterPreviewContent(team: KboTeam) {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = team,
                games = previewBaseballGames.filter { it.homeTeam == team || it.awayTeam == team },
                isLoading = false,
                hasLoadedSchedule = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
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
                selectedTeam = KboTeam.NC,
                isLoading = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
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
                selectedTeam = KboTeam.NC,
                isLoading = false,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(name = "경기 일정 없음", showBackground = true)
@Composable
private fun BaseballScheduleEmptyPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                isLoading = false,
                hasLoadedSchedule = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(name = "경기 일정 갱신 중", showBackground = true)
@Composable
private fun BaseballScheduleRefreshingPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                games = previewBaseballGames,
                isLoading = false,
                isRefreshing = true,
                hasLoadedSchedule = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(name = "빈 경기 일정 갱신 중", showBackground = true)
@Composable
private fun BaseballScheduleEmptyRefreshingPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                isLoading = false,
                isRefreshing = true,
                hasLoadedSchedule = true,
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(name = "갱신 실패 후 경기 일정 유지", showBackground = true)
@Composable
private fun BaseballScheduleRefreshErrorPreview() {
    YadanbeopseokTheme {
        BaseballScheduleScreen(
            uiState = BaseballScheduleUiState(
                selectedTeam = KboTeam.KIA,
                games = previewBaseballGames,
                isLoading = false,
                hasLoadedSchedule = true,
                errorMessage = "인터넷 연결을 확인한 후 다시 시도해주세요.",
            ),
            onTeamSelected = {},
            onPlanClick = {},
            onRetryClick = {},
            onRefreshClick = {},
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

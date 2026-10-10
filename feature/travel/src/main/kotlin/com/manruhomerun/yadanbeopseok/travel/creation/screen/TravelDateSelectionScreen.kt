package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanIconButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryInk
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextSecondary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.MAX_TRAVEL_NIGHTS
import com.manruhomerun.yadanbeopseok.travel.creation.viewmodel.isValidDateRange
import java.time.YearMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus

/**
 * B·05 여행 만들기의 여행 기간 선택 화면입니다.
 *
 * 선택한 직관 경기일을 포함하면서 최대 2박 3일까지 선택할 수 있습니다.
 */
@Composable
fun TravelDateSelectionScreen(
    selectedGame: BaseballGame,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingStartDateValue by rememberSaveable(selectedGame.id) {
        mutableStateOf<String?>(null)
    }

    val pendingStartDate = pendingStartDateValue?.let { value ->
        LocalDate.parse(value)
    }

    TravelDateSelectionContent(
        selectedGame = selectedGame,
        startDate = startDate,
        endDate = endDate,
        pendingStartDate = pendingStartDate,
        onDateClick = { clickedDate ->
            val currentStartDate = pendingStartDate

            if (currentStartDate == null) {
                pendingStartDateValue = clickedDate.toString()
            } else {
                val resolvedStartDate = minOf(currentStartDate, clickedDate)
                val resolvedEndDate = maxOf(currentStartDate, clickedDate)

                if (isValidDateRange(selectedGame, resolvedStartDate, resolvedEndDate)) {
                    onDateRangeSelected(resolvedStartDate, resolvedEndDate)
                    pendingStartDateValue = null
                }
            }
        },
        onCancelSelection = { pendingStartDateValue = null },
        onBackClick = onBackClick,
        onNextClick = onNextClick,
        modifier = modifier,
    )
}

/** 확정된 기간과 임시 선택을 구분해 달력 및 선택 안내를 표시합니다. */
@Composable
private fun TravelDateSelectionContent(
    selectedGame: BaseballGame,
    startDate: LocalDate?,
    endDate: LocalDate?,
    pendingStartDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    onCancelSelection: () -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gameDate = selectedGame.gameDateTime.date
    val displayedStartDate = pendingStartDate ?: startDate
    val displayedEndDate = if (pendingStartDate == null) endDate else null
    val isNextEnabled = startDate != null &&
        endDate != null &&
        pendingStartDate == null &&
        isValidDateRange(selectedGame, startDate, endDate)

    // 첫 선택 전에는 경기일과 구성 가능한 날짜를, 선택 중에는 유효한 두 번째 날짜를 허용합니다.
    val selectionAnchor = pendingStartDate ?: gameDate

    TravelCreationScaffold(
        currentStep = TravelCreationStep.DATE_SELECTION,
        title = "언제 다녀올까요?",
        description = "${gameDate.toShortDateText()} 직관 경기일을 포함해\n최대 2박 3일까지 선택할 수 있어요.",
        onNavigationClick = onBackClick,
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
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
            )
        },
    ) {
        item(key = "calendar") {
            TravelMonthCalendar(
                gameDate = gameDate,
                selectedStartDate = displayedStartDate,
                selectedEndDate = displayedEndDate ?: displayedStartDate,
                isDateEnabled = { date ->
                    isValidDateRange(
                        selectedGame,
                        minOf(selectionAnchor, date),
                        maxOf(selectionAnchor, date),
                    )
                },
                onDateClick = onDateClick,
            )
        }

        item(key = "selected_period") {
            SelectedTravelPeriodCard(
                startDate = displayedStartDate,
                endDate = displayedEndDate,
                onCancelSelection = onCancelSelection.takeIf { pendingStartDate != null },
            )
        }

        item(key = "calendar_legend") {
            TravelCalendarLegend()
        }
    }
}

/**
 * 선택 경기일을 기준으로 여행 기간을 고르는 월간 달력입니다.
 */
@Composable
private fun TravelMonthCalendar(
    gameDate: LocalDate,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    isDateEnabled: (LocalDate) -> Boolean,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val earliestDate = remember(gameDate) {
        gameDate.minus(MAX_TRAVEL_NIGHTS, DateTimeUnit.DAY)
    }
    val latestDate = remember(gameDate) {
        gameDate.plus(MAX_TRAVEL_NIGHTS, DateTimeUnit.DAY)
    }

    val earliestMonth = earliestDate.toYearMonth()
    val latestMonth = latestDate.toYearMonth()

    var displayedYear by rememberSaveable(gameDate.toString()) {
        mutableIntStateOf(gameDate.year)
    }
    var displayedMonthNumber by rememberSaveable(gameDate.toString()) {
        mutableIntStateOf(gameDate.month.number)
    }

    val displayedMonth = YearMonth.of(
        displayedYear,
        displayedMonthNumber,
    )
    val calendarCells = remember(displayedMonth) {
        displayedMonth.toCalendarCells()
    }

    YadanCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        top = 12.dp,
                        end = 10.dp,
                        bottom = 10.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${displayedMonth.year}. ${displayedMonth.monthValue}",
                    modifier = Modifier.weight(1f),
                    style = YadanTypography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanTextPrimary,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    YadanIconButton(
                        onClick = {
                            val previousMonth = displayedMonth.minusMonths(1)
                            displayedYear = previousMonth.year
                            displayedMonthNumber = previousMonth.monthValue
                        },
                        style = YadanIconButtonStyle.TONAL,
                        size = YadanIconButtonSize.MEDIUM,
                        enabled = displayedMonth > earliestMonth,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "이전 달",
                            )
                        },
                    )

                    YadanIconButton(
                        onClick = {
                            val nextMonth = displayedMonth.plusMonths(1)
                            displayedYear = nextMonth.year
                            displayedMonthNumber = nextMonth.monthValue
                        },
                        style = YadanIconButtonStyle.TONAL,
                        size = YadanIconButtonSize.MEDIUM,
                        enabled = displayedMonth < latestMonth,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "다음 달",
                            )
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                KOREAN_WEEKDAY_LABELS.forEachIndexed { index, label ->
                    Text(
                        text = label,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 7.dp),
                        style = YadanTypography.labelSmall,
                        color = if (index == 0) {
                            YadanError
                        } else {
                            YadanTextMuted
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                calendarCells.chunked(DAYS_PER_WEEK).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            TravelCalendarDay(
                                date = date,
                                gameDate = gameDate,
                                selectedStartDate = selectedStartDate,
                                selectedEndDate = selectedEndDate,
                                enabled = date != null &&
                                    date in earliestDate..latestDate &&
                                    isDateEnabled(date),
                                onClick = {
                                    date?.let(onDateClick)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
private fun TravelCalendarDay(
    date: LocalDate?,
    gameDate: LocalDate,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (date == null) {
        Box(modifier = modifier.aspectRatio(1f))
        return
    }

    val isRangeSelected = selectedStartDate != null &&
        selectedEndDate != null &&
        date in selectedStartDate..selectedEndDate
    val isGameDate = date == gameDate

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics {
                contentDescription = date.toAccessibleDateText(isGameDate)
                this.selected = isRangeSelected
            }
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (isRangeSelected) {
                        YadanPrimary
                    } else {
                        Color.Transparent
                    },
                    shape = MaterialTheme.shapes.small,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = date.day.toString(),
                style = YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = when {
                    isRangeSelected -> YadanOnPrimary
                    !enabled -> YadanTextMuted.copy(alpha = 0.45f)
                    else -> YadanTextPrimary
                },
            )

            Box(
                modifier = Modifier
                    .size(GAME_DATE_MARKER_SIZE)
                    .background(
                        color = when {
                            !isGameDate -> Color.Transparent
                            isRangeSelected -> YadanOnPrimary
                            else -> YadanPrimary
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

/**
 * 현재 선택 중이거나 선택이 완료된 여행 기간을 표시합니다.
 */
@Composable
private fun SelectedTravelPeriodCard(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onCancelSelection: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val periodText = when {
        startDate == null -> "날짜를 선택해주세요"
        endDate == null -> "${startDate.toShortDateText()} · 두 번째 날짜를 선택해주세요"
        else -> "${startDate.toShortDateText()} ~ ${endDate.toShortDateText()}"
    }

    YadanCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "선택한 기간",
                    style = YadanTypography.labelSmall,
                    color = YadanTextSecondary,
                )

                Text(
                    text = periodText,
                    style = YadanTypography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = YadanTextPrimary,
                )
            }

            if (onCancelSelection != null) {
                TextButton(onClick = onCancelSelection) {
                    Text(
                        text = "선택 취소",
                        style = YadanTypography.labelSmall,
                        color = YadanPrimaryInk,
                    )
                }
            } else if (startDate != null && endDate != null) {
                val nights = endDate.toEpochDays() - startDate.toEpochDays()

                YadanStatusChip(
                    text = if (nights == 0L) {
                        "당일"
                    } else {
                        "${nights}박 ${nights + 1}일"
                    },
                    style = YadanStatusChipStyle.PRIMARY,
                )
            }
        }
    }
}

/**
 * 여행 기간과 직관 경기일의 달력 표시를 설명합니다.
 */
@Composable
private fun TravelCalendarLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 2.dp,
                top = 2.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarLegendItem(
            label = "여행 기간",
            marker = {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = YadanPrimary,
                            shape = MaterialTheme.shapes.extraSmall,
                        ),
                )
            },
        )

        CalendarLegendItem(
            label = "직관 경기일",
            marker = {
                Box(
                    modifier = Modifier
                        .size(GAME_DATE_MARKER_SIZE)
                        .background(
                            color = YadanPrimary,
                            shape = CircleShape,
                        ),
                )
            },
        )
    }
}

@Composable
private fun CalendarLegendItem(
    label: String,
    marker: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        marker()

        Text(
            text = label,
            style = YadanTypography.labelSmall,
            color = YadanTextSecondary,
        )
    }
}

private fun YearMonth.toCalendarCells(): List<LocalDate?> {
    val leadingEmptyCellCount = atDay(1).dayOfWeek.value % DAYS_PER_WEEK
    val dayCount = lengthOfMonth()
    val occupiedCellCount = leadingEmptyCellCount + dayCount
    val cellCount = if (occupiedCellCount <= FIVE_WEEK_CELL_COUNT) {
        FIVE_WEEK_CELL_COUNT
    } else {
        SIX_WEEK_CELL_COUNT
    }

    return List(cellCount) { cellIndex ->
        val day = cellIndex - leadingEmptyCellCount + 1

        if (day in 1..dayCount) {
            LocalDate(
                year = year,
                month = monthValue,
                day = day,
            )
        } else {
            null
        }
    }
}

private fun LocalDate.toYearMonth(): YearMonth = YearMonth.of(year, month.number)

private fun LocalDate.toShortDateText(): String {
    val weekday = KOREAN_WEEKDAY_LABELS[(dayOfWeek.ordinal + 1) % DAYS_PER_WEEK]
    return "${month.number}.$day ($weekday)"
}

private fun LocalDate.toAccessibleDateText(isGameDate: Boolean): String {
    val suffix = if (isGameDate) " 직관 경기일" else ""
    return "${year}년 ${month.number}월 ${day}일$suffix"
}

private val KOREAN_WEEKDAY_LABELS = listOf(
    "일",
    "월",
    "화",
    "수",
    "목",
    "금",
    "토",
)

private const val DAYS_PER_WEEK = 7
private const val FIVE_WEEK_CELL_COUNT = 35
private const val SIX_WEEK_CELL_COUNT = 42
private val GAME_DATE_MARKER_SIZE = 8.dp

@Preview(
    name = "B05 여행 기간",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelDateSelectionScreenPreview() {
    TravelDateSelectionPreview(
        startDate = LocalDate(2026, 5, 22),
        endDate = LocalDate(2026, 5, 23),
    )
}

@Preview(name = "B05 기간 미선택", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelDateSelectionEmptyPreview() {
    TravelDateSelectionPreview()
}

@Preview(name = "B05 두 번째 날짜 선택", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelDateSelectionPendingPreview() {
    TravelDateSelectionPreview(pendingStartDate = LocalDate(2026, 5, 22))
}

@Preview(name = "B05 당일 선택", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelDateSelectionDayTripPreview() {
    TravelDateSelectionPreview(
        startDate = LocalDate(2026, 5, 23),
        endDate = LocalDate(2026, 5, 23),
    )
}

@Preview(name = "B05 2박 3일 선택", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelDateSelectionMaximumPeriodPreview() {
    TravelDateSelectionPreview(
        startDate = LocalDate(2026, 5, 22),
        endDate = LocalDate(2026, 5, 24),
    )
}

@Preview(name = "B05 연도 경계", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TravelDateSelectionYearBoundaryPreview() {
    TravelDateSelectionPreview(
        startDate = LocalDate(2026, 12, 31),
        endDate = LocalDate(2027, 1, 2),
        gameDate = LocalDate(2027, 1, 1),
    )
}

@Preview(
    name = "B05 좁은 화면 재선택",
    showBackground = true,
    widthDp = 320,
    heightDp = 740,
    fontScale = 1.3f,
)
@Composable
private fun TravelDateSelectionNarrowPreview() {
    TravelDateSelectionPreview(
        startDate = LocalDate(2026, 5, 22),
        endDate = LocalDate(2026, 5, 23),
        pendingStartDate = LocalDate(2026, 5, 21),
    )
}

@Composable
private fun TravelDateSelectionPreview(
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    pendingStartDate: LocalDate? = null,
    gameDate: LocalDate = LocalDate(2026, 5, 23),
) {
    val game = BaseballGame(
        id = "game-123",
        stadium = BaseballStadium(
            id = "stadium-9",
            name = "사직야구장",
            region = Region.BUSAN,
            latitude = 35.1940,
            longitude = 129.0615,
        ),
        homeTeam = KboTeam.LOTTE,
        awayTeam = KboTeam.KIA,
        gameDateTime = LocalDateTime(
            year = gameDate.year,
            month = gameDate.month.number,
            day = gameDate.day,
            hour = 17,
            minute = 0,
        ),
        gameType = BaseballGameType.REGULAR,
    )

    YadanbeopseokTheme {
        TravelDateSelectionContent(
            selectedGame = game,
            startDate = startDate,
            endDate = endDate,
            pendingStartDate = pendingStartDate,
            onDateClick = {},
            onCancelSelection = {},
            onBackClick = {},
            onNextClick = {},
        )
    }
}

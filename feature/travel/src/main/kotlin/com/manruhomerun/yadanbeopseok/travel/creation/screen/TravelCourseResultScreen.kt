package com.manruhomerun.yadanbeopseok.travel.creation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButton
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanButtonStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanTopAppBar
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanError
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanSurface
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballGameType
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.travel.component.TravelScheduleContent
import com.manruhomerun.yadanbeopseok.travel.util.toDisplayDay
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDateRangeText
import com.manruhomerun.yadanbeopseok.travel.util.toTravelDayDateText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * B·07에서 서버가 생성한 추천 여행 코스를 최종 확인하는 화면입니다.
 *
 * 이름 변경 방식은 화면에서 결정하지 않고 [onRenameClick]로 전달합니다.
 * 일정 수정과 최종 저장 역시 Route에서 실제 동작을 연결합니다.
 */
@Composable
fun TravelCourseResultScreen(
    course: TravelCourse,
    game: BaseballGame,
    travelName: String,
    startDate: LocalDate,
    endDate: LocalDate,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    onEditScheduleClick: (() -> Unit)?,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {

    val displayDays = remember(course, game) {
        course.days.map { travelDay ->
            travelDay.toDisplayDay(
                baseballGame = course.baseballGame,
                game = game,
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(YadanBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        YadanTopAppBar(
            title = "추천 여행",
            onNavigationClick = {
                if (!isSaving) {
                    onBackClick()
                }
            },
        )

        TravelScheduleContent(
            title = travelName,
            dateText = startDate.toTravelDateRangeText(endDate),
            isLeader = true,
            visibleTravelDays = displayDays,
            dateTextForDay = { day -> startDate.toTravelDayDateText(day) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            sectionTitle = "방문 순서",
            onRenameClick = onRenameClick,
            enabled = !isSaving,
            emptyMessage = "생성된 여행 일정이 없습니다",
        )

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 8.dp,
                    ),
                style = YadanTypography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = YadanError,
                textAlign = TextAlign.Center,
            )
        }

        TravelCourseResultBottomBar(
            isSaving = isSaving,
            isSaveEnabled = travelName.isNotBlank() && course.days.isNotEmpty(),
            onEditScheduleClick = onEditScheduleClick,
            onSaveClick = onSaveClick,
        )
    }
}

/**
 * 추천 결과의 일정 수정 및 최종 저장 작업을 제공합니다.
 */
@Composable
private fun TravelCourseResultBottomBar(
    isSaving: Boolean,
    isSaveEnabled: Boolean,
    onEditScheduleClick: (() -> Unit)?,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(YadanSurface)
            .padding(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 20.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        YadanButton(
            text = "일정 수정",
            onClick = {
                onEditScheduleClick?.invoke()
            },
            modifier = Modifier.weight(1f),
            style = YadanButtonStyle.GHOST,
            enabled = !isSaving && onEditScheduleClick != null,
        )

        YadanButton(
            text = "이대로 저장",
            onClick = onSaveClick,
            modifier = Modifier.weight(1.35f),
            enabled = isSaveEnabled,
            isLoading = isSaving,
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

@Preview(
    name = "B07 추천 여행 결과",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseResultScreenPreview() {
    YadanbeopseokTheme {
        TravelCourseResultScreen(
            course = previewTravelCourse(),
            game = previewBaseballGame(),
            travelName = "부산 사직 직관 여행",
            startDate = LocalDate(2026, 5, 22),
            endDate = LocalDate(2026, 5, 23),
            isSaving = false,
            onBackClick = {},
            onRenameClick = {},
            onEditScheduleClick = {},
            onSaveClick = {},
        )
    }
}

@Preview(
    name = "B07 추천 여행 저장 중",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun TravelCourseResultSavingPreview() {
    YadanbeopseokTheme {
        TravelCourseResultScreen(
            course = previewTravelCourse(),
            game = previewBaseballGame(),
            travelName = "부산 사직 직관 여행",
            startDate = LocalDate(2026, 5, 22),
            endDate = LocalDate(2026, 5, 23),
            isSaving = true,
            onBackClick = {},
            onRenameClick = {},
            onEditScheduleClick = {},
            onSaveClick = {},
        )
    }
}

private fun previewTravelCourse(): TravelCourse =
    TravelCourse(
        baseballGame = TravelBaseballGame(
            id = "game-123",
            day = 1,
            baseballGameAfterIdx = 1,
        ),
        days = listOf(
            TravelDay(
                day = 1,
                places = listOf(
                    previewTravelPlace(
                        id = "spot-1",
                        name = "감천문화마을",
                        category = TravelSpotCategory.CULTURE,
                        order = 1,
                    ),
                    previewTravelPlace(
                        id = "spot-3",
                        name = "광안리 해수욕장",
                        category = TravelSpotCategory.NATURE,
                        order = 3,
                    ),
                ),
            ),
            TravelDay(
                day = 2,
                places = listOf(
                    previewTravelPlace(
                        id = "spot-4",
                        name = "해운대 오션뷰 호텔",
                        category = TravelSpotCategory.ACCOMMODATION,
                        order = 1,
                    ),
                    previewTravelPlace(
                        id = "spot-5",
                        name = "부평깡통시장",
                        category = TravelSpotCategory.FOOD,
                        order = 2,
                    ),
                ),
            ),
        ),
    )

private fun previewTravelPlace(
    id: String,
    name: String,
    category: TravelSpotCategory,
    order: Int,
): TravelPlace =
    TravelPlace(
        spot = TravelSpot(
            id = id,
            name = name,
            category = category,
        ),
        order = order,
    )

private fun previewBaseballGame(): BaseballGame = BaseballGame(
    id = "game-123",
    stadium = BaseballStadium(
        id = "preview-stadium",
        name = "사직야구장",
        region = Region.BUSAN,
        latitude = 35.194,
        longitude = 129.061,
    ),
    homeTeam = KboTeam.LOTTE,
    awayTeam = KboTeam.KIA,
    gameDateTime = LocalDateTime(2026, 5, 22, 17, 0),
    gameType = BaseballGameType.REGULAR,
)

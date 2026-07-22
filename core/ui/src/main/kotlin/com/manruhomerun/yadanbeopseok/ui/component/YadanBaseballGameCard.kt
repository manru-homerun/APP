package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCardStyle
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanRadioButton
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.Region
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

/**
 * 경기 일정 화면에서 경기 정보와 여행 생성 버튼을 표시하는 카드입니다.
 *
 * A·05 경기 일정 화면의 HTML `.gamex`에 대응합니다.
 * 날짜별 제목은 목록에서 별도로 표시하므로 카드 안에는 경기 시간만 표시합니다.
 *
 * @param game 표시할 야구 경기입니다.
 * @param onPlanClick 여행 짜기 버튼을 눌렀을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param enabled 여행 짜기 버튼의 활성화 여부입니다.
 */
@Composable
fun YadanGameScheduleCard(
    game: BaseballGame,
    onPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    YadanCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        YadanBaseballGameCardContent(
            game = game,
            dateTimeText = game.gameDateTime.toTimeText(),
            bottomTrailingContent = {
                YadanGamePlanButton(
                    onClick = onPlanClick,
                    enabled = enabled,
                )
            },
        )
    }
}

/**
 * 여행 생성 과정에서 하나의 경기를 선택하는 카드입니다.
 *
 * B·01 경기 선택 화면의 HTML `.gamex.gpick`에 대응합니다.
 * 카드 전체에 단일 선택 동작과 RadioButton 접근성을 제공합니다.
 *
 * 여러 카드를 함께 표시하는 화면에서는 부모 목록에
 * `Modifier.selectableGroup()`을 적용하는 것이 좋습니다.
 *
 * @param game 표시할 야구 경기입니다.
 * @param selected 현재 경기의 선택 여부입니다.
 * @param onClick 카드를 눌렀을 때 실행할 작업입니다.
 * @param modifier 카드의 크기와 배치를 지정할 Modifier입니다.
 * @param enabled 카드 선택 가능 여부입니다.
 */
@Composable
fun YadanGameSelectionCard(
    game: BaseballGame,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    YadanCard(
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(
                    if (enabled) {
                        1f
                    } else {
                        0.42f
                    },
                )
                .clip(MaterialTheme.shapes.large)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        style =
            if (selected) {
                YadanCardStyle.SELECTED
            } else {
                YadanCardStyle.DEFAULT
            },
    ) {
        YadanBaseballGameCardContent(
            game = game,
            dateTimeText = game.gameDateTime.toSelectionDateTimeText(),
            topTrailingContent = {
                /*
                 * 카드 전체에서 선택 동작을 처리하므로 라디오 버튼은
                 * 별도의 클릭 영역을 만들지 않고 선택 상태만 표시합니다.
                 */
                YadanRadioButton(
                    selected = selected,
                    onClick = null,
                    enabled = enabled,
                )
            },
        )
    }
}

/**
 * 일정 카드와 경기 선택 카드가 공유하는 경기 정보 영역입니다.
 */
@Composable
private fun YadanBaseballGameCardContent(
    game: BaseballGame,
    dateTimeText: String,
    topTrailingContent: (@Composable () -> Unit)? = null,
    bottomTrailingContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier.padding(
                horizontal = 14.dp,
                vertical = 13.dp,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateTimeText,
                modifier = Modifier.weight(1f),
                style =
                    YadanTypography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = YadanTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (topTrailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                topTrailingContent()
            }
        }

        YadanGameMatchup(
            homeTeam = game.homeTeam,
            awayTeam = game.awayTeam,
            modifier =
                Modifier.padding(
                    top = 12.dp,
                    bottom = 12.dp,
                ),
        )

        HorizontalDivider(
            thickness = 1.5.dp,
            color = YadanDivider,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YadanGameVenue(
                venueName = game.stadium.name,
                modifier = Modifier.weight(1f),
            )

            if (bottomTrailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                bottomTrailingContent()
            }
        }
    }
}

/**
 * 경기장 아이콘과 경기장 이름을 표시합니다.
 */
@Composable
private fun YadanGameVenue(
    venueName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = YadanPrimary,
        )

        Text(
            text = venueName,
            style =
                YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = YadanTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 경기 일정 카드에서 여행 생성 화면으로 이동하는 작은 버튼입니다.
 *
 * 기존 YadanButton은 화면의 주요 작업을 위한 56dp 버튼이므로,
 * 카드 내부의 작은 작업은 Material Button과 디자인 토큰을 조합합니다.
 */
@Composable
private fun YadanGamePlanButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(9.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = YadanPrimary,
                contentColor = YadanOnPrimary,
                disabledContainerColor = YadanPrimary.copy(alpha = 0.42f),
                disabledContentColor = YadanOnPrimary.copy(alpha = 0.72f),
            ),
        elevation = null,
        contentPadding =
            PaddingValues(
                horizontal = 10.dp,
                vertical = 0.dp,
            ),
    ) {
        Text(
            text = "여행 짜기",
            style =
                YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
        )

        Spacer(modifier = Modifier.width(3.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
    }
}

/**
 * 경기 시간을 `17:00` 형태로 반환합니다.
 */
private fun LocalDateTime.toTimeText(): String =
    "${hour.toTwoDigitText()}:${minute.toTwoDigitText()}"

/**
 * 경기 선택 카드의 날짜와 시간을 `5.23 (토) · 17:00` 형태로 반환합니다.
 */
private fun LocalDateTime.toSelectionDateTimeText(): String =
    "${month.number}.$day (${dayOfWeek.toKoreanShortName()}) · ${toTimeText()}"

/**
 * 요일을 한글 한 글자로 반환합니다.
 */
private fun DayOfWeek.toKoreanShortName(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

/**
 * 한 자리 숫자 앞에 0을 붙여 두 자리 문자열로 반환합니다.
 */
private fun Int.toTwoDigitText(): String =
    toString().padStart(
        length = 2,
        padChar = '0',
    )

@Preview(
    name = "Yadan baseball game cards",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanBaseballGameCardPreview() {
    val game =
        BaseballGame(
            id = "game-1",
            stadium =
                BaseballStadium(
                    id = "stadium-1",
                    name = "사직야구장",
                    region = Region.BUSAN,
                    latitude = 35.194,
                    longitude = 129.061,
                ),
            homeTeam = KboTeam.LOTTE,
            awayTeam = KboTeam.KIA,
            gameDateTime = LocalDateTime(2026, 5, 23, 17, 0),
            gameType =
                com.manruhomerun.yadanbeopseok.model.BaseballGameType.REGULAR,
        )

    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "경기 일정",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanGameScheduleCard(
                game = game,
                onPlanClick = {},
            )

            Text(
                text = "경기 선택",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            YadanGameSelectionCard(
                game = game,
                selected = true,
                onClick = {},
            )

            YadanGameSelectionCard(
                game =
                    game.copy(
                        id = "game-2",
                        gameDateTime = LocalDateTime(2026, 5, 24, 14, 0),
                    ),
                selected = false,
                onClick = {},
            )
        }
    }
}

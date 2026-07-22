package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanCard
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChip
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipSize
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanStatusChipStyle
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPrimaryGradient
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTextMuted
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.ui.theme.teamColor

/**
 * 경기 대진이 표시되는 배경에 따른 시각적 스타일입니다.
 */
enum class YadanGameMatchupStyle {
    /**
     * 밝은 카드에서 사용합니다.
     *
     * 구단명에는 각 구단의 대표 상징색을 사용하고
     * VS 문구에는 기본 보조 텍스트 색상을 적용합니다.
     */
    DEFAULT,

    /**
     * 어두운 배경이나 그라데이션 위에서 사용합니다.
     *
     * HTML 히어로 여행 카드처럼 구단명은 흰색,
     * VS 문구는 반투명 흰색으로 표시합니다.
     */
    ON_DARK,
}

/**
 * 야구 경기의 홈팀과 원정팀 대진을 표시하는 공통 컴포넌트입니다.
 *
 * 경기 일정 카드, 경기 선택 카드와 홈의 여행 카드에서 공통으로 사용합니다.
 * 사용하는 배경에 따라 [style]을 지정할 수 있습니다.
 *
 * @param homeTeam 홈팀입니다.
 * @param awayTeam 원정팀입니다.
 * @param modifier 컴포넌트의 크기와 배치를 지정할 Modifier입니다.
 * @param style 대진 정보의 색상과 크기 스타일입니다.
 * @param showHomeIndicator 홈팀 옆에 홈 표시를 노출할지 결정합니다.
 */
@Composable
fun YadanGameMatchup(
    homeTeam: KboTeam,
    awayTeam: KboTeam,
    modifier: Modifier = Modifier,
    style: YadanGameMatchupStyle = YadanGameMatchupStyle.DEFAULT,
    showHomeIndicator: Boolean = true,
) {
    val matchupSpacing =
        when (style) {
            YadanGameMatchupStyle.DEFAULT -> 10.dp
            YadanGameMatchupStyle.ON_DARK -> 9.dp
        }

    val versusColor =
        when (style) {
            YadanGameMatchupStyle.DEFAULT -> YadanTextMuted
            YadanGameMatchupStyle.ON_DARK ->
                YadanOnPrimary.copy(alpha = 0.72f)
        }

    val versusTextStyle =
        when (style) {
            YadanGameMatchupStyle.DEFAULT ->
                YadanTypography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                )

            YadanGameMatchupStyle.ON_DARK ->
                YadanTypography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Normal,
                )
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(matchupSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        YadanMatchupTeam(
            team = homeTeam,
            isHomeTeam = true,
            style = style,
            showHomeIndicator = showHomeIndicator,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "VS",
            style = versusTextStyle,
            color = versusColor,
        )

        YadanMatchupTeam(
            team = awayTeam,
            isHomeTeam = false,
            style = style,
            showHomeIndicator = showHomeIndicator,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 대진 영역 안에서 하나의 구단 로고와 구단명을 표시합니다.
 *
 * 홈팀이며 홈 표시가 활성화된 경우 구단명 옆에 작은 홈 상태 칩을 추가합니다.
 *
 * @param team 표시할 KBO 구단입니다.
 * @param isHomeTeam 홈팀 여부입니다.
 * @param style 구단명에 적용할 색상과 크기 스타일입니다.
 * @param showHomeIndicator 홈 표시 노출 여부입니다.
 * @param modifier 구단 영역의 크기와 배치를 지정할 Modifier입니다.
 */
@Composable
private fun YadanMatchupTeam(
    team: KboTeam,
    isHomeTeam: Boolean,
    style: YadanGameMatchupStyle,
    showHomeIndicator: Boolean,
    modifier: Modifier = Modifier,
) {
    val teamContentSpacing =
        when (style) {
            YadanGameMatchupStyle.DEFAULT -> 8.dp
            YadanGameMatchupStyle.ON_DARK -> 6.dp
        }

    val teamNameColor =
        when (style) {
            YadanGameMatchupStyle.DEFAULT -> team.teamColor
            YadanGameMatchupStyle.ON_DARK -> YadanOnPrimary
        }

    val teamNameTextStyle =
        when (style) {
            YadanGameMatchupStyle.DEFAULT ->
                YadanTypography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                )

            YadanGameMatchupStyle.ON_DARK ->
                YadanTypography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                )
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement =
                Arrangement.spacedBy(teamContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            /*
             * HTML의 작은 구단 로고 크기인 28px에 대응합니다.
             * 옆의 구단명이 같은 정보를 전달하므로 설명은 생략합니다.
             */
            YadanTeamLogo(
                team = team,
                contentDescription = null,
                size = 28.dp,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = team.displayName,
                    style = teamNameTextStyle,
                    color = teamNameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (isHomeTeam && showHomeIndicator) {
                    YadanStatusChip(
                        text = "홈",
                        style = YadanStatusChipStyle.TINTED,
                        size = YadanStatusChipSize.SMALL,
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Yadan game matchups",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
)
@Composable
private fun YadanGameMatchupPreview() {
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
                text = "기본 스타일 · 홈 표시 있음",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            /*
             * 경기 일정 카드와 경기 선택 카드에서 사용하는 형태입니다.
             * 기존 크기와 구단별 대표 색상을 그대로 유지합니다.
             */
            YadanCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                YadanGameMatchup(
                    homeTeam = KboTeam.LOTTE,
                    awayTeam = KboTeam.KIA,
                    modifier =
                        Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 16.dp,
                        ),
                )
            }

            Text(
                text = "어두운 배경 · 홈 표시 없음",
                style = YadanTypography.labelMedium,
                color = YadanTextMuted,
            )

            /*
             * 여행 카드의 그라데이션 배경과 반투명 대진 영역을
             * Preview에서 함께 확인합니다.
             */
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            brush = YadanPrimaryGradient,
                            shape = MaterialTheme.shapes.large,
                        )
                        .padding(14.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.Black.copy(alpha = 0.16f),
                    border =
                        BorderStroke(
                            width = 1.5.dp,
                            color = YadanOnPrimary.copy(alpha = 0.14f),
                        ),
                ) {
                    YadanGameMatchup(
                        homeTeam = KboTeam.LOTTE,
                        awayTeam = KboTeam.KIA,
                        style = YadanGameMatchupStyle.ON_DARK,
                        showHomeIndicator = false,
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 9.dp,
                            ),
                    )
                }
            }
        }
    }
}

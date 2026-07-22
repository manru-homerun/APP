package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanOnPrimary
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanPillShape
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanTypography
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.ui.theme.teamColor

/**
 * 사용자의 응원 구단을 구단 대표 색상으로 표시하는 배지입니다.
 *
 * 친구 목록, 동행자 선택, 마이페이지와 프로필 화면에서 재사용합니다.
 *
 * @param team 표시할 KBO 구단입니다.
 * @param modifier 배지의 크기와 배치를 지정합니다.
 */
@Composable
fun YadanTeamBadge(
    team: KboTeam,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier.clearAndSetSemantics {
                contentDescription =
                    "${team.fullName} 응원 구단"
            },
        shape = YadanPillShape,
        color = team.teamColor,
        contentColor = YadanOnPrimary,
    ) {
        Text(
            text = team.displayName,
            modifier =
                Modifier.padding(
                    horizontal = 7.dp,
                    vertical = 2.dp,
                ),
            style =
                YadanTypography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview(
    name = "Yadan team badges",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanTeamBadgePreview() {
    YadanbeopseokTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(YadanBackground)
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KboTeam.entries
                .chunked(5)
                .forEach { teams ->
                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        teams.forEach { team ->
                            YadanTeamBadge(team = team)
                        }
                    }
                }
        }
    }
}

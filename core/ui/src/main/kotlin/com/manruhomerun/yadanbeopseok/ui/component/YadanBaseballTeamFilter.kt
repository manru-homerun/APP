package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanBackground
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanDivider
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanShapes
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * 경기 일정 조회에 사용할 KBO 구단 필터입니다.
 *
 * 서버 구단 ID 순서로 구단을 표시하며, 전체 경기 일정 API가 없으므로
 * 전체 필터는 제공하지 않습니다.
 *
 * @param selectedTeam 현재 선택된 구단입니다.
 * @param onTeamSelected 구단을 선택했을 때 실행할 작업입니다.
 * @param modifier 외부에서 크기와 배치를 지정할 Modifier입니다.
 * @param enabled 구단 필터의 활성화 여부입니다.
 */
@Composable
fun YadanBaseballTeamFilter(
    selectedTeam: KboTeam?,
    onTeamSelected: (KboTeam) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
            key = KboTeam::serverId,
        ) { team ->
            YadanFilterChip(
                text = team.displayName,
                selected = selectedTeam == team,
                onClick = {
                    onTeamSelected(team)
                },
                enabled = enabled,
            )
        }
    }
}

@Preview(
    name = "KBO 구단 필터",
    showBackground = true,
    backgroundColor = 0xFFFAFAFA,
    widthDp = 390,
)
@Composable
private fun YadanBaseballTeamFilterPreview() {
    var selectedTeam by remember {
        mutableStateOf<KboTeam?>(KboTeam.KIA)
    }

    YadanbeopseokTheme {
        YadanBaseballTeamFilter(
            selectedTeam = selectedTeam,
            onTeamSelected = { team ->
                selectedTeam = team
            },
            modifier = Modifier.background(YadanBackground),
        )
    }
}

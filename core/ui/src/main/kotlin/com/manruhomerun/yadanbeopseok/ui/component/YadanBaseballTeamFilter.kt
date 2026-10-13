package com.manruhomerun.yadanbeopseok.ui.component

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manruhomerun.yadanbeopseok.designsystem.component.YadanFilterChip
import com.manruhomerun.yadanbeopseok.designsystem.theme.YadanbeopseokTheme
import com.manruhomerun.yadanbeopseok.model.KboTeam
import kotlinx.coroutines.flow.first

/**
 * 경기 일정 조회에 사용할 KBO 구단 필터입니다.
 *
 * 서버 구단 ID 순서로 구단을 표시하며, 전체 경기 일정 API가 없으므로
 * 전체 필터는 제공하지 않습니다.
 * 최초 선택과 구단 변경 때만 필요한 만큼 위치를 보정하며,
 * 동일한 선택값에서는 사용자가 스크롤하거나 복원한 위치를 유지합니다.
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
    val selectedTeamIndex = teams.indexOf(selectedTeam)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedTeamIndex.coerceAtLeast(0))
    var lastHandledTeamId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(selectedTeam) {
        val teamId = selectedTeam?.serverId ?: return@LaunchedEffect
        if (selectedTeamIndex < 0 || lastHandledTeamId == teamId) return@LaunchedEffect

        val layoutInfo = snapshotFlow { listState.layoutInfo }.first {
            it.totalItemsCount == teams.size &&
                it.visibleItemsInfo.isNotEmpty() &&
                it.viewportEndOffset > it.viewportStartOffset
        }

        // 사용자 조작으로 보정을 생략하거나 취소해도 같은 선택값으로 다시 끌어오지 않습니다.
        lastHandledTeamId = teamId
        if (listState.isScrollInProgress) return@LaunchedEffect

        val scrollOffset = layoutInfo.teamVisibilityScrollOffset(teamId)
        when {
            scrollOffset == null -> listState.scrollToItem(selectedTeamIndex)
            scrollOffset != 0f -> listState.scrollBy(scrollOffset)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
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

/** 현재 보이는 구단을 드러내는 최소 이동량이며, 화면 밖의 구단은 null을 반환합니다. */
private fun LazyListLayoutInfo.teamVisibilityScrollOffset(teamId: Long): Float? {
    val item = visibleItemsInfo.firstOrNull { it.key == teamId } ?: return null
    return when {
        item.offset < viewportStartOffset -> (item.offset - viewportStartOffset).toFloat()
        item.offset + item.size > viewportEndOffset -> (item.offset + item.size - viewportEndOffset).toFloat()
        else -> 0f
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
        )
    }
}

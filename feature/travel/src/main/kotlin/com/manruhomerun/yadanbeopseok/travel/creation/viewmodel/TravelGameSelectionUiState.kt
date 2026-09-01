package com.manruhomerun.yadanbeopseok.travel.creation.viewmodel

import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * B·01 경기 선택 화면에서 사용하는 상태입니다.
 *
 * 경기 목록과 로딩 상태는 B·01에서만 사용하며, 최종 확정된 경기 상세 정보는
 * 공유 [TravelCreationUiState]에 저장합니다.
 */
data class TravelGameSelectionUiState(
    /** 현재 경기 일정 필터에서 선택한 구단입니다. */
    val selectedTeam: KboTeam? = null,

    /** 선택한 구단의 요청일 기준 최대 2주 경기 일정입니다. */
    val games: List<BaseballGameSummary> = emptyList(),

    /** 사용자가 선택한 경기의 고유 식별자입니다. */
    val selectedGameId: String? = null,

    /** 선택한 구단의 경기 일정을 불러오고 있는지 나타냅니다. */
    val isScheduleLoading: Boolean = false,

    /** 선택한 경기의 상세 정보를 불러오고 있는지 나타냅니다. */
    val isGameDetailLoading: Boolean = false,

    /** 경기 조회 과정에서 표시할 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
) {
    /** 현재 경기 목록에서 사용자가 선택한 경기입니다. */
    val selectedGame: BaseballGameSummary?
        get() = games.firstOrNull { it.id == selectedGameId }

    /** 선택한 경기를 확정하고 다음 단계로 이동할 수 있는지 나타냅니다. */
    val isContinueEnabled: Boolean
        get() = selectedGame != null && !isScheduleLoading && !isGameDetailLoading
}

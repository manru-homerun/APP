package com.manruhomerun.yadanbeopseok.baseball.viewmodel

import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * A·05 경기 일정 화면에서 사용하는 UI 상태입니다.
 *
 * 선택한 구단과 해당 구단의 요청일 기준 최대 2주 경기 일정을 관리합니다.
 */
data class BaseballScheduleUiState(
    /** 현재 일정 필터에서 선택한 구단입니다. */
    val selectedTeam: KboTeam = KboTeam.entries.minBy { it.serverId },

    /** 선택한 구단의 요청일 기준 최대 2주 경기 일정입니다. */
    val games: List<BaseballGameSummary> = emptyList(),

    /** 경기 일정을 불러오는 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 경기 일정 조회 중 발생한 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
)

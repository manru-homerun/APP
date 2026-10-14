package com.manruhomerun.yadanbeopseok.baseball.viewmodel

import com.manruhomerun.yadanbeopseok.model.BaseballGameSummary
import com.manruhomerun.yadanbeopseok.model.KboTeam

/**
 * A·05 경기 일정 화면에서 사용하는 UI 상태입니다.
 *
 * 선택한 구단과 해당 구단의 요청일 기준 최대 2주 경기 일정을 관리합니다.
 */
data class BaseballScheduleUiState(
    /** 현재 선택한 구단이며, 기본 진입에서는 맨 앞의 두산을 선택합니다. */
    val selectedTeam: KboTeam = KboTeam.DOOSAN,

    /** 선택한 구단의 요청일 기준 최대 2주 경기 일정입니다. */
    val games: List<BaseballGameSummary> = emptyList(),

    /** 최초 조회 또는 다른 구단의 경기 일정을 불러오는 중인지 나타냅니다. */
    val isLoading: Boolean = true,

    /** 기존 조회 결과를 유지하면서 현재 구단의 경기 일정을 갱신하는 중인지 나타냅니다. */
    val isRefreshing: Boolean = false,

    /** 현재 구단의 조회가 성공했는지 나타내며, 빈 응답도 성공으로 처리합니다. */
    val hasLoadedSchedule: Boolean = false,

    /** 경기 일정 조회 중 발생한 사용자 안내 문구입니다. */
    val errorMessage: String? = null,
)

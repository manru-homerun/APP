package com.manruhomerun.yadanbeopseok.record.viewmodel

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSummary

/**
 * D01 여행 기록 화면에 표시할 상태입니다.
 *
 * 전체 조회가 완료된 여행에서 시즌별 통계를 계산하고 목록은 나누어 표시합니다.
 *
 * @property completedTravels 보관된 완료 여행 목록. 최초 조회 중에는 일부 페이지일 수 있습니다.
 * @property selectedSeason 현재 화면에서 선택한 시즌 연도
 * @property displayedTravelCount 선택 시즌에서 화면에 표시할 최대 여행 수
 * @property hasCompleteStatistics 보관된 목록의 전체 페이지 조회가 성공했는지 여부
 * @property isLoading 전체 조회 또는 새로고침을 진행 중인지 여부
 * @property errorMessage 전체 조회 또는 갱신 실패 메시지
 */
data class TravelRecordUiState(
    val completedTravels: List<TravelSummary> = emptyList(),
    val selectedSeason: Int? = null,
    val displayedTravelCount: Int = TRAVEL_RECORD_PAGE_SIZE,
    val hasCompleteStatistics: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    /**
     * 조회된 여행의 시작 연도를 기준으로 선택 가능한 시즌을 만듭니다.
     */
    val availableSeasons: List<Int>
        get() = completedTravels
            .map { travel -> travel.startDate.year }
            .distinct()
            .sortedDescending()

    /**
     * 현재 선택한 시즌에 해당하는 완료 여행만 반환합니다.
     *
     * 아직 시즌이 선택되지 않았다면 조회된 전체 여행을 반환합니다.
     */
    val seasonTravels: List<TravelSummary>
        get() {
            val season = selectedSeason ?: return completedTravels

            return completedTravels.filter { travel ->
                travel.startDate.year == season
            }
        }

    /** 목록에 표시할 여행만 반환하며 통계 집계에는 사용하지 않습니다. */
    val visibleTravels: List<TravelSummary>
        get() = seasonTravels.take(displayedTravelCount)

    /**
     * 카카오맵에 표시할 지역별 완료 여행 횟수입니다.
     */
    val regionVisitCounts: Map<Region, Int>
        get() = if (hasCompleteStatistics) {
            seasonTravels.groupingBy { travel -> travel.region }.eachCount()
        } else {
            emptyMap()
        }

    /**
     * 선택한 시즌의 전체 완료 여행 개수이며 미완성 집계는 null입니다.
     */
    val completedTravelCount: Int?
        get() = seasonTravels.size.takeIf { hasCompleteStatistics }

    /**
     * 조회는 끝났지만 표시할 완료 여행이 없는 상태인지 나타냅니다.
     */
    val isEmpty: Boolean
        get() = hasCompleteStatistics && completedTravels.isEmpty()

    /** 서버 조회 상태와 무관하게 보관된 목록에서 더 표시할 여행이 있는지 검사합니다. */
    val hasNextPage: Boolean
        get() = displayedTravelCount < seasonTravels.size
}

internal const val TRAVEL_RECORD_PAGE_SIZE = 10

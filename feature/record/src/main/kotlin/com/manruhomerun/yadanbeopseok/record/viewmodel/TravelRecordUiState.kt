package com.manruhomerun.yadanbeopseok.record.viewmodel

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSummary

/**
 * D01 여행 기록 화면에 표시할 상태입니다.
 *
 * 완료된 여행 목록에서 선택 시즌의 여행과 지역별 방문 횟수를 계산합니다.
 *
 * @property completedTravels 서버에서 조회한 완료 여행 목록
 * @property selectedSeason 현재 화면에서 선택한 시즌 연도
 * @property pageNumber 마지막으로 불러온 페이지 번호
 * @property totalPages 전체 페이지 수
 * @property isLoading 첫 페이지를 불러오는 중인지 여부
 * @property isLoadingMore 다음 페이지를 불러오는 중인지 여부
 * @property errorMessage 사용자에게 표시할 오류 메시지
 * @property loadMoreErrorMessage 다음 페이지 조회 실패 메시지
 */
data class TravelRecordUiState(
    val completedTravels: List<TravelSummary> = emptyList(),
    val selectedSeason: Int? = null,
    val pageNumber: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
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
    val visibleTravels: List<TravelSummary>
        get() {
            val season = selectedSeason ?: return completedTravels

            return completedTravels.filter { travel ->
                travel.startDate.year == season
            }
        }

    /**
     * 카카오맵에 표시할 지역별 완료 여행 횟수입니다.
     */
    val regionVisitCounts: Map<Region, Int>
        get() = visibleTravels
            .groupingBy { travel -> travel.region }
            .eachCount()

    /**
     * 선택한 시즌에 포함된 완료 여행 개수입니다.
     */
    val completedTravelCount: Int
        get() = visibleTravels.size

    /**
     * 조회는 끝났지만 표시할 완료 여행이 없는 상태인지 나타냅니다.
     */
    val isEmpty: Boolean
        get() = !isLoading &&
            errorMessage == null &&
            visibleTravels.isEmpty()

    /** 완료 여행 목록에 조회할 다음 페이지가 있는지 나타냅니다. */
    val hasNextPage: Boolean
        get() = pageNumber < totalPages
}

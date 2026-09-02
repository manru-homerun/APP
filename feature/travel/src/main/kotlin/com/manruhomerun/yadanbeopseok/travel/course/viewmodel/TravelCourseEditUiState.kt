package com.manruhomerun.yadanbeopseok.travel.course.viewmodel

import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import kotlinx.datetime.LocalDate

/**
 * C01 여행 일정 편집과 C02 저장 완료 화면에서 사용하는 상태입니다.
 *
 * B07의 저장 전 코스를 편집하는 경우 [travelId]는 null이고,
 * 기존 여행을 편집하는 경우에는 저장된 여행 ID를 보관합니다.
 *
 * @property travelId 수정 중인 저장된 여행 ID
 * @property travelName 저장하거나 수정할 여행 이름
 * @property startDate 여행 시작일
 * @property endDate 여행 종료일
 * @property companionCount 여행에 참여하는 동행자 수
 * @property baseballGame 화면에 표시할 야구 경기 상세 정보
 * @property course 현재 편집 중인 여행 코스
 * @property isLoading 기존 여행과 경기 정보를 불러오는 중인지 여부
 * @property isAligning 여행 코스를 재정렬하는 중인지 여부
 * @property isSaving 여행 코스를 저장하는 중인지 여부
 * @property errorMessage 사용자에게 표시할 오류 메시지
 */
data class TravelCourseEditUiState(
    val travelId: String? = null,
    val travelName: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val companionCount: Int = 0,
    val baseballGame: BaseballGame? = null,
    val course: TravelCourse? = null,
    val isLoading: Boolean = false,
    val isAligning: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    /** 저장된 여행을 수정하는 흐름인지 나타냅니다. */
    val isExistingTravel: Boolean
        get() = travelId != null

    /**
     * 여행 코스에 포함된 관광지 수입니다.
     *
     * 별도로 관리하는 야구 경기는 관광지 수에 포함하지 않습니다.
     */
    val travelSpotCount: Int
        get() = course?.days?.sumOf { day -> day.places.size } ?: 0

    /** 재정렬이나 저장 요청이 진행 중인지 나타냅니다. */
    val isRequestInProgress: Boolean
        get() = isAligning || isSaving

    /** C01 편집 화면에 필요한 정보가 준비됐는지 나타냅니다. */
    val hasContent: Boolean
        get() = startDate != null &&
            endDate != null &&
            baseballGame != null &&
            course != null

    /** 현재 편집 내용을 저장할 수 있는지 나타냅니다. */
    val canSave: Boolean
        get() = hasContent &&
            travelName.isNotBlank() &&
            !isLoading &&
            !isRequestInProgress
}

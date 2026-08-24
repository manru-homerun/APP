package com.manruhomerun.yadanbeopseok.travel.viewmodel

import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelCompanionCondition
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import com.manruhomerun.yadanbeopseok.model.UserProfile
import kotlinx.datetime.LocalDate

/**
 * 여행 만들기 전체 화면에서 공유하는 입력 및 요청 상태입니다.
 *
 * 경기 선택부터 추천 코스 저장까지 화면 사이에서 유지해야 하는 값만 관리합니다.
 * 각 화면에서 조회하는 경기, 친구, 테마 및 관광지 목록은 해당 화면 상태에서
 * 별도로 관리합니다.
 */
data class TravelCreationUiState(
    /** B·01에서 선택한 경기의 상세 정보입니다. */
    val selectedGame: BaseballGame? = null,

    /** B·02에서 선택한 동행 조건 목록입니다. */
    val selectedCompanionConditions: Set<TravelCompanionCondition> = emptySet(),

    /** B·03에서 선택한 여행 테마 목록입니다. */
    val selectedThemes: List<TravelTheme> = emptyList(),

    /** B·04에서 선택한 동행자 목록입니다. */
    val selectedCompanions: List<UserProfile> = emptyList(),

    /** B·05에서 선택한 여행 시작일입니다. */
    val startDate: LocalDate? = null,

    /** B·05에서 선택한 여행 종료일입니다. */
    val endDate: LocalDate? = null,

    /** B·06에서 코스에 반드시 포함하도록 선택한 관광지 목록입니다. */
    val selectedTravelSpots: List<TravelSpot> = emptyList(),

    /** 서버가 생성한 저장 전 여행 코스입니다. */
    val generatedCourse: TravelCourse? = null,

    /** B·07에서 표시하고 수정할 여행 이름입니다. */
    val travelName: String = "",

    /** AI 여행 코스를 생성하고 있는지 나타냅니다. */
    val isGenerating: Boolean = false,

    /** 최종 여행 코스를 저장하고 있는지 나타냅니다. */
    val isSaving: Boolean = false,

    /** 여행 생성 또는 저장 중 표시할 오류 메시지입니다. */
    val errorMessage: String? = null,
)

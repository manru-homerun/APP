package com.manruhomerun.yadanbeopseok.travel.util

import com.manruhomerun.yadanbeopseok.model.BaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory

/**
 * 관광지와 야구 경기를 같은 순서 목록으로 표현합니다.
 * 조회 화면과 편집 화면에서 동일한 경기 배치 규칙을 사용합니다.
 */
internal sealed interface TravelCourseTimelineItem {
    data class Place(val place: TravelPlace) : TravelCourseTimelineItem

    data object BaseballGame : TravelCourseTimelineItem
}

/**
 * 코스에서 해당 일차를 찾아 공통 타임라인 구성 함수를 호출합니다.
 * 존재하지 않는 일차라면 null을 반환합니다.
 */
internal fun TravelCourse.toTimelineItems(day: Int): MutableList<TravelCourseTimelineItem>? {
    val travelDay = days.firstOrNull { it.day == day } ?: return null
    return travelDay.toTimelineItems(baseballGame)
}

/**
 * 관광지를 방문 순서로 정렬하고, 경기 일차라면 지정 위치에 경기를 삽입합니다.
 *
 * baseballGameAfterIdx는 0부터 시작하는 삽입 위치입니다.
 * 반환 목록은 새로 생성하므로 목록을 편집해도 원본 일정은 변경되지 않습니다.
 */
internal fun TravelDay.toTimelineItems(baseballGame: TravelBaseballGame): MutableList<TravelCourseTimelineItem> {
    val timelineItems = places
        .sortedBy { it.order }
        .mapTo(mutableListOf<TravelCourseTimelineItem>()) { place ->
            TravelCourseTimelineItem.Place(place)
        }

    if (baseballGame.day == day) {
        val gameIndex = baseballGame.baseballGameAfterIdx.coerceIn(0, timelineItems.size)
        timelineItems.add(gameIndex, TravelCourseTimelineItem.BaseballGame)
    }

    return timelineItems
}

/**
 * 실제 경기의 구장 정보를 기존 일정 카드용 장소로 변환합니다.
 * 화면 표시 전용이며, 서버에 저장할 관광지 목록에는 추가하지 않습니다.
 */
internal fun BaseballGame.toSchedulePlace(): TravelPlace = TravelPlace(
    spot = TravelSpot(
        id = stadium.id,
        name = stadium.name,
        region = stadium.region,
        category = TravelSpotCategory.STADIUM,
    ),
    order = 0,
)

/**
 * 경기 배치 위치를 반영한 조회용 일정을 만듭니다.
 * 공통 UI가 order로 정렬하므로 경기 삽입 후 표시 순서를 다시 부여합니다.
 * 원본 일정은 변경하지 않으며 관광지의 인증 상태도 그대로 유지합니다.
 */
internal fun TravelDay.toDisplayDay(baseballGame: TravelBaseballGame, game: BaseballGame): TravelDay {
    val displayPlaces = toTimelineItems(baseballGame).mapIndexed { index, item ->
        val place = when (item) {
            is TravelCourseTimelineItem.Place -> item.place
            TravelCourseTimelineItem.BaseballGame -> game.toSchedulePlace()
        }

        place.copy(order = index + 1)
    }

    return copy(places = displayPlaces)
}

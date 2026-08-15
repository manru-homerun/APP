package com.manruhomerun.yadanbeopseok.model

/**
 * 앱 내부에서 사용하는 관광지 모델입니다.
 *
 * ERD의 travel_spot을 참고합니다.
 *
 * @property dibs 현재 사용자가 해당 관광지를 찜했는지 여부
 */
data class TravelSpot(
    val id: String,
    val name: String,
    val address: String? = null,
    val region: Region? = null,
    val category: TravelSpotCategory,
    val imageUrl: String? = null,
    val dibs: Boolean = false,
)

enum class TravelSpotCategory(
    val displayName: String,
) {
    ACCOMMODATION("숙박"),
    FESTIVAL("행사"),
    EXPERIENCE("체험"),
    FOOD("음식"),
    HISTORY("역사"),
    LEISURE("레저"),
    NATURE("자연"),
    SHOPPING("쇼핑"),
    CULTURE("문화"),
    STADIUM("직관"),
    UNKNOWN("기타"),
}

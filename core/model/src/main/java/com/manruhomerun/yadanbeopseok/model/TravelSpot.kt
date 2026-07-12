package com.manruhomerun.yadanbeopseok.model

/**
 * 앱 내부에서 사용하는 여행지 모델입니다.
 *
 * ERD의 travel_spot을 참고합니다.
 * 찜 여부는 like 테이블 자체가 아니라 화면에서 바로 쓰기 좋은 isLiked 값으로 표현합니다.
 */
data class TravelSpot(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val region: Region,
    val category: TravelSpotCategory,
    val imageUrl: String? = null,
    val isLiked: Boolean = false,
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

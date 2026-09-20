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

/**
 * 관광지 목록과 서버의 페이지 정보를 함께 보관하는 앱 내부 모델입니다.
 *
 * @property travelSpots 현재 페이지에 포함된 관광지 목록
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 한 페이지에 포함되는 최대 관광지 수
 * @property totalElements 조회 조건에 해당하는 전체 관광지 수
 * @property totalPages 전체 페이지 수
 */
data class TravelSpotListPage(
    val travelSpots: List<TravelSpot>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)

/**
 * 관광지 상세 화면에서 사용하는 앱 내부 모델입니다.
 *
 * 목록에서 사용하는 [TravelSpot]을 재사용하고 상세 API와 이미지 목록 API에서
 * 추가로 받은 정보만 함께 보관합니다.
 *
 * @property spot 관광지의 기본 정보와 현재 사용자의 찜 상태
 * @property telephone 관광지 전화번호
 * @property homepage 관광지 홈페이지 정보
 * @property longitude 관광지 경도
 * @property latitude 관광지 위도
 * @property overview 관광지 상세 소개
 * @property imageUrls 상세 화면 갤러리에 표시할 이미지 URL 목록
 */
data class TravelSpotDetail(
    val spot: TravelSpot,
    val telephone: String? = null,
    val homepage: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val overview: String? = null,
    val imageUrls: List<String> = emptyList(),
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
    TOURIST_ATTRACTION("관광지"),
    TRAVEL_COURSE("여행코스"),
    SHOPPING("쇼핑"),
    CULTURE("문화"),
    STADIUM("직관"),
    UNKNOWN("기타"),
}

/**
 * 인기 관광지와 찜한 관광지를 서버에 요청할 때 사용하는 필터 카테고리입니다.
 *
 * enum 이름은 백엔드 요청 값과 동일하며, [displayName]은 화면 필터에 표시합니다.
 */
enum class TravelSpotFilterCategory(
    val displayName: String,
) {
    ACCOMMODATION("숙박"),
    FESTIVAL_PERFORMANCE_EVENT("행사"),
    RESTAURANT("음식"),
    LEPORTS("레저"),
    TOURIST_ATTRACTION("관광지"),
    SHOPPING("쇼핑"),
    CULTURAL_FACILITY("문화"),
    TRAVEL_COURSE("여행코스"),
}

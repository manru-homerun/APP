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
    SHOPPING("쇼핑"),
    CULTURE("문화"),
    STADIUM("직관"),
    UNKNOWN("기타"),
}

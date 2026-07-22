package com.manruhomerun.yadanbeopseok.model

/**
 * 사용자 여행 프로필에서 사용하는 시도 단위 지역입니다.
 *
 * 기존 [Region]은 야구장, 여행지와 여행 코스의 지역을 나타내고,
 * [ProfileRegion]은 사용자의 거주 지역과 선호 여행 지역을 나타냅니다.
 *
 * 거주 지역과 선호 여행 지역은 같은 시도 코드 체계를 사용하지만
 * 데이터에서 선택할 수 있는 지역 목록이 서로 다르므로
 * 각 용도의 사용 가능 여부를 함께 관리합니다.
 *
 * @property code 데이터 및 API에서 사용하는 시도 코드입니다.
 * @property displayName 화면에 표시할 지역명입니다.
 * @property isAvailableForResidence 거주 지역으로 선택할 수 있는지 나타냅니다.
 * @property isAvailableForPreferredTravel 선호 여행 지역으로 선택할 수 있는지 나타냅니다.
 */
enum class ProfileRegion(
    val code: String,
    val displayName: String,
    val isAvailableForResidence: Boolean = true,
    val isAvailableForPreferredTravel: Boolean = true,
) {
    SEOUL(
        code = "11",
        displayName = "서울",
    ),
    BUSAN(
        code = "26",
        displayName = "부산",
    ),
    DAEGU(
        code = "27",
        displayName = "대구",
    ),
    INCHEON(
        code = "28",
        displayName = "인천",
    ),
    GWANGJU(
        code = "29",
        displayName = "광주",
    ),
    DAEJEON(
        code = "30",
        displayName = "대전",
    ),
    ULSAN(
        code = "31",
        displayName = "울산",
        isAvailableForResidence = false,
    ),
    SEJONG(
        code = "36",
        displayName = "세종",
        isAvailableForPreferredTravel = false,
    ),
    GYEONGGI(
        code = "41",
        displayName = "경기",
    ),
    GANGWON(
        code = "42",
        displayName = "강원",
    ),
    CHUNGBUK(
        code = "43",
        displayName = "충북",
    ),
    CHUNGNAM(
        code = "44",
        displayName = "충남",
    ),
    JEONBUK(
        code = "45",
        displayName = "전북",
    ),
    JEONNAM(
        code = "46",
        displayName = "전남",
    ),
    GYEONGBUK(
        code = "47",
        displayName = "경북",
    ),
    GYEONGNAM(
        code = "48",
        displayName = "경남",
    ),
    JEJU(
        code = "50",
        displayName = "제주",
        isAvailableForResidence = false,
    ),
    ;

    companion object {
        /**
         * 거주 지역으로 선택할 수 있는 지역 목록입니다.
         */
        val residenceOptions: List<ProfileRegion> =
            entries.filter { region ->
                region.isAvailableForResidence
            }

        /**
         * 선호 여행 지역으로 선택할 수 있는 지역 목록입니다.
         */
        val preferredTravelOptions: List<ProfileRegion> =
            entries.filter { region ->
                region.isAvailableForPreferredTravel
            }

        /**
         * 시도 코드와 일치하는 프로필 지역을 반환합니다.
         *
         * 서버에서 알 수 없는 코드가 전달되면 null을 반환합니다.
         */
        fun findByCode(code: String): ProfileRegion? =
            entries.firstOrNull { region ->
                region.code == code
            }
    }
}

package com.manruhomerun.yadanbeopseok.model

/**
 * 앱에서 지원하는 야구 여행 지역입니다.
 *
 * 현재는 KBO 구장이 있는 주요 도시 중심으로 관리합니다.
 * legalDongCode는 백엔드/API 및 관광 데이터 조회에서 사용하는 지역 코드입니다.
 */
enum class Region(
    val displayName: String,
    val legalDongCode: String,
) {
    SEOUL("서울특별시", "1100000000"),
    SUWON("수원", "4111000000"),
    INCHEON("인천광역시", "2800000000"),
    DAEJEON("대전", "3000000000"),
    DAEGU("대구", "2700000000"),
    GWANGJU("광주", "1200000000"),
    BUSAN("부산", "2600000000"),
    CHANGWON("창원", "4812000000");

    companion object {
        fun findByLegalDongCode(code: String): Region? =
            entries.firstOrNull { it.legalDongCode == code }
    }
}

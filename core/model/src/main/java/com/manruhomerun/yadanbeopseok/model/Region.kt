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
    SEOUL("서울특별시", "11000"),
    SUWON("수원", "41110"),
    INCHEON("인천광역시", "28000"),
    DAEJEON("대전", "30000"),
    DAEGU("대구", "27000"),
    GWANGJU("광주", "12000"),
    BUSAN("부산", "26000"),
    CHANGWON("창원", "48120");

    companion object {
        fun findByLegalDongCode(code: String): Region? =
            entries.firstOrNull { it.legalDongCode == code }
    }
}

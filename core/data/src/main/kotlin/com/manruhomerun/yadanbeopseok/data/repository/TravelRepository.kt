package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.TravelListPage

/**
 * 여행 데이터를 제공하는 Repository입니다.
 *
 * 현재는 홈 화면에서 필요한 진행 중·예정 여행 목록 조회만 정의합니다.
 * 이후 여행 상세나 여행 만들기 기능을 개발할 때 관련 함수를 이 인터페이스에
 * 순차적으로 추가합니다.
 */
interface TravelRepository {
    /**
     * 진행 중 여행과 진행 예정 여행 목록을 함께 조회합니다.
     *
     * 구현체는 여행 목록 API에 PLANNED 상태를 전달합니다.
     * 각 여행의 진행 중·예정 여부는 시작일과 종료일을 기준으로
     * 홈 계층에서 구분합니다.
     */
    suspend fun getPlannedTravels(): TravelListPage
}

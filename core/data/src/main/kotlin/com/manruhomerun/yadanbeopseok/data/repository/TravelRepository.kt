package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelListPage

/**
 * 여행 목록과 상세 데이터를 제공하는 Repository입니다.
 *
 * Network DTO를 외부에 노출하지 않고 앱 내부 여행 모델로 변환해 제공합니다.
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

    /**
     * 여행 ID에 해당하는 상세 일정과 방문 인증 상태를 조회합니다.
     *
     * @param travelId 조회할 여행의 고유 식별자
     */
    suspend fun getTravel(travelId: String): Travel
}

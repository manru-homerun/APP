package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.TravelCertification
import kotlinx.datetime.LocalDateTime

/**
 * 여행 일정에 포함된 관광지의 방문 인증을 처리합니다.
 */
interface TravelRecordRepository {
    /**
     * 현재 위치와 방문 시각을 전달하여 관광지 방문 인증을 요청합니다.
     *
     * 인증 결과만 반환하며, 여행 진행률 갱신이나 스티커 조회는
     * 이 함수에서 실행하지 않습니다.
     *
     * @param travelId 인증할 여행 ID
     * @param spotId 인증할 관광지 ID
     * @param latitude 현재 위치의 위도
     * @param longitude 현재 위치의 경도
     * @param visitedAt 방문 인증 요청 시각
     * @param accuracy 위치 정확도(미터). 확인할 수 없으면 null
     */
    suspend fun verifyTravelSpot(
        travelId: String,
        spotId: String,
        latitude: Double,
        longitude: Double,
        visitedAt: LocalDateTime,
        accuracy: Double? = null,
    ): TravelCertification
}

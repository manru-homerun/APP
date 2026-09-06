package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravelCertification
import com.manruhomerun.yadanbeopseok.data.repository.TravelRecordRepository
import com.manruhomerun.yadanbeopseok.model.TravelCertification
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelApi
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotVerifyRequestDto
import javax.inject.Inject
import kotlinx.datetime.LocalDateTime

/**
 * 관광지 방문 인증을 요청하고 서버의 인증 결과를 반환합니다.
 *
 * 위치 측정, 여행 진행률 갱신과 스티커 조회는 담당하지 않습니다.
 */
internal class TravelRecordRepositoryImpl @Inject constructor(
    private val travelApi: TravelApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelRecordRepository {
    override suspend fun verifyTravelSpot(
        travelId: String,
        spotId: String,
        latitude: Double,
        longitude: Double,
        visitedAt: LocalDateTime,
        accuracy: Double?,
    ): TravelCertification {
        val request = TravelSpotVerifyRequestDto(
            latitude = latitude,
            longitude = longitude,
            visitedAt = visitedAt.toString(),
            accuracy = accuracy,
        )

        val response = apiCallExecutor.execute {
            travelApi.verifyTravelSpot(
                travelId = travelId,
                spotId = spotId,
                request = request,
            )
        }

        return response.requireData().toTravelCertification()
    }
}

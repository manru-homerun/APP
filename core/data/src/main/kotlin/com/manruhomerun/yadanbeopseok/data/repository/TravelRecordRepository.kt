package com.manruhomerun.yadanbeopseok.data.repository

import com.manruhomerun.yadanbeopseok.model.BaseballStadium
import com.manruhomerun.yadanbeopseok.model.TravelCertification
import kotlinx.datetime.LocalDateTime

interface TravelRecordRepository {
    /**
     * GPS 기반으로 특정 경기장 방문을 인증합니다.
     */
    suspend fun verifyStadiumVisit(
        stadiumId: String,
        latitude: Double,
        longitude: Double,
    ): StadiumVisitCertification

    /**
     * 시즌별 경기장 방문 기록 지도에 표시할 데이터를 조회합니다.
     */
    suspend fun getStadiumVisitRecords(
        year: Int,
    ): List<StadiumVisitRecord>

    /**
     * 여행 코스에 포함된 특정 관광지의 GPS 방문 인증을 요청합니다.
     */
    suspend fun verifyTravelSpot(
        travelId: String,
        spotId: String,
        latitude: Double,
        longitude: Double,
    ): TravelCertification

    /**
     * 관광지 방문 인증 후 스티커 사진 생성을 위한 이미지를 업로드합니다.
     */
    suspend fun uploadTravelRecordImage(
        params: TravelRecordImageUploadParams,
    ): UploadedTravelRecordImage
}

data class StadiumVisitCertification(
    val stadiumId: String,
    val certificatedAt: LocalDateTime,
)

data class StadiumVisitRecord(
    val stadium: BaseballStadium,
    val visitCount: Int,
    val lastVisitedAt: LocalDateTime?,
)

data class TravelRecordImageUploadParams(
    val travelId: String,
    val spotId: String,
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray,
)

data class UploadedTravelRecordImage(
    val imageUrl: String,
)

package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpot
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpotDetail
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelSpotApi
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotDibsRequestDto
import javax.inject.Inject

/**
 * 관광지 조회, 검색, 추천 및 찜 API의 Repository 구현체입니다.
 */
internal class TravelSpotRepositoryImpl @Inject constructor(
    private val travelSpotApi: TravelSpotApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelSpotRepository {
    override suspend fun getPopularTravelSpots(region: Region): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.getPopularTravelSpots(region = region)
        }

        return response.requireData().content.map { dto ->
            dto.toTravelSpot()
        }
    }

    override suspend fun getSuggestedTravelSpots(region: Region): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.getSuggestedTravelSpots(region = region)
        }

        return response.requireData().map { dto ->
            dto.toTravelSpot()
        }
    }

    override suspend fun searchTravelSpots(searchKeyword: String): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.searchTravelSpots(searchKeyword = searchKeyword)
        }

        return response.requireData().contents.map { dto ->
            dto.toTravelSpot()
        }
    }

    override suspend fun getTravelSpotDetail(spotId: String): TravelSpotDetail {
        val contentId = spotId.toContentId()

        val detailResponse = apiCallExecutor.execute {
            travelSpotApi.getTravelSpotDetail(spotId = contentId)
        }

        val imagesResponse = apiCallExecutor.execute {
            travelSpotApi.getTravelSpotImages(spotId = contentId)
        }

        return detailResponse.requireData().toTravelSpotDetail(
            imageUrls = imagesResponse.requireData(),
        )
    }

    override suspend fun getTravelSpotDibs(region: Region?): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.getTravelSpotDibs(region = region)
        }

        return response.requireData().map { dto ->
            dto.toTravelSpot(defaultDibs = true)
        }
    }

    override suspend fun addTravelSpotDibs(spotId: String) {
        val request = TravelSpotDibsRequestDto(contentId = spotId.toContentId())

        apiCallExecutor.execute {
            travelSpotApi.addTravelSpotDibs(request = request)
        }
    }

    override suspend fun deleteTravelSpotDibs(spotId: String) {
        val request = TravelSpotDibsRequestDto(contentId = spotId.toContentId())

        apiCallExecutor.execute {
            travelSpotApi.deleteTravelSpotDibs(request = request)
        }
    }
}

/**
 * 앱 내부의 문자열 관광지 ID를 API에서 사용하는 숫자 ID로 변환합니다.
 */
private fun String.toContentId(): Long =
    toLongOrNull()
        ?: throw IllegalArgumentException(
            "Travel spot ID must be numeric.",
        )

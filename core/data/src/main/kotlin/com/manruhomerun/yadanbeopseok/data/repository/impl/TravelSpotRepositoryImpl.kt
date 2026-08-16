package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpot
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpotDetail
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelApi
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotDibsRequestDto
import javax.inject.Inject

/**
 * 인기 관광지, 관광지 상세와 찜 API의 Repository 구현체입니다.
 */
internal class TravelSpotRepositoryImpl @Inject constructor(
    private val travelApi: TravelApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelSpotRepository {
    override suspend fun getPopularTravelSpots(
        region: Region,
    ): List<TravelSpot> {
        val response = apiCallExecutor.execute {
                travelApi.getPopularTravelSpots(
                    region = region,
                )
            }

        return response.requireData().content.map { dto ->
            dto.toTravelSpot()
        }
    }

    override suspend fun getTravelSpotDetail(spotId: String): TravelSpotDetail {
        val contentId = spotId.toContentId()

        val detailResponse = apiCallExecutor.execute {
            travelApi.getTravelSpotDetail(spotId = contentId)
        }

        val imagesResponse = apiCallExecutor.execute {
            travelApi.getTravelSpotImages(spotId = contentId)
        }

        return detailResponse.requireData().toTravelSpotDetail(
            imageUrls = imagesResponse.requireData(),
        )
    }

    override suspend fun getTravelSpotDibs(
        region: Region?,
    ): List<TravelSpot> {
        val response =
            apiCallExecutor.execute {
                travelApi.getTravelSpotDibs(
                    region = region,
                )
            }

        return response.requireData().map { dto ->
            dto.toTravelSpot(
                defaultDibs = true,
            )
        }
    }

    override suspend fun addTravelSpotDibs(spotId: String) {
        apiCallExecutor.execute {
            travelApi.addTravelSpotDibs(
                request =
                    TravelSpotDibsRequestDto(
                        contentId = spotId.toContentId(),
                    ),
            )
        }
    }

    override suspend fun deleteTravelSpotDibs(spotId: String) {
        apiCallExecutor.execute {
            travelApi.deleteTravelSpotDibs(
                request =
                    TravelSpotDibsRequestDto(
                        contentId = spotId.toContentId(),
                    ),
            )
        }
    }
}

/** 앱의 문자열 관광지 ID를 API의 Long contentId로 변환합니다. */
private fun String.toContentId(): Long =
    toLongOrNull()
        ?: throw IllegalArgumentException(
            "Travel spot ID must be numeric.",
        )

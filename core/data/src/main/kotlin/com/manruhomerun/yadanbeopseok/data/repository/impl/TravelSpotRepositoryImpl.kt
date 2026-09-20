package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.common.SessionExpiredException
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpot
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpotDetail
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpotListPage
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpotSuggestionRequestDto
import com.manruhomerun.yadanbeopseok.data.repository.SuggestTravelSpotsParams
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotListPage
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelSpotApi
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * 관광지 조회, 검색, 추천 및 찜 API의 Repository 구현체입니다.
 */
internal class TravelSpotRepositoryImpl @Inject constructor(
    private val travelSpotApi: TravelSpotApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelSpotRepository {
    override suspend fun getPopularTravelSpots(
        region: Region,
        category: TravelSpotFilterCategory?,
    ): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.getPopularTravelSpots(
                region = region,
                category = category,
            )
        }

        return response.contents.map { dto ->
            dto.toTravelSpot(region = region)
        }
    }

    override suspend fun getSuggestedTravelSpots(params: SuggestTravelSpotsParams): List<TravelSpot> {
        val request = params.toTravelSpotSuggestionRequestDto()

        val response = apiCallExecutor.execute {
            travelSpotApi.getSuggestedTravelSpots(request = request)
        }

        return response.contents.map { dto ->
            dto.toTravelSpot(region = params.region)
        }
    }

    override suspend fun searchTravelSpots(searchKeyword: String, region: Region): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.searchTravelSpots(
                searchKeyword = searchKeyword,
                region = region,
            )
        }

        return response.contents.map { dto ->
            dto.toTravelSpot()
        }
    }

    override suspend fun getTravelSpotDetail(spotId: String): TravelSpotDetail {
        val detailResponse = apiCallExecutor.execute {
            travelSpotApi.getTravelSpotDetail(contentId = spotId)
        }

        val imagesResponse = try {
            apiCallExecutor.execute {
                travelSpotApi.getTravelSpotImages(contentId = spotId)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SessionExpiredException) {
            throw exception
        } catch (_: Exception) {
            emptyList()
        }

        return detailResponse.toTravelSpotDetail(imageUrls = imagesResponse)
    }

    override suspend fun getTravelSpotDibs(
        region: Region,
        category: TravelSpotFilterCategory?,
        pageNumber: Int,
        pageSize: Int,
    ): TravelSpotListPage {
        val response = apiCallExecutor.execute {
            travelSpotApi.getTravelSpotDibs(
                region = region,
                category = category,
                pageNumber = pageNumber,
                pageSize = pageSize,
            )
        }

        return response.toTravelSpotListPage(defaultDibs = true)
    }

    override suspend fun addTravelSpotDibs(spotId: String) {
        apiCallExecutor.execute {
            travelSpotApi.addTravelSpotDibs(contentId = spotId)
        }
    }

    override suspend fun deleteTravelSpotDibs(spotId: String) {
        apiCallExecutor.execute {
            travelSpotApi.deleteTravelSpotDibs(contentId = spotId)
        }
    }
}

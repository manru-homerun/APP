package com.manruhomerun.yadanbeopseok.data.repository.impl

import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpot
import com.manruhomerun.yadanbeopseok.data.mapper.toTravelSpotDetail
import com.manruhomerun.yadanbeopseok.data.repository.TravelSpotRepository
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.model.TravelSpotFilterCategory
import com.manruhomerun.yadanbeopseok.network.common.error.ApiCallExecutor
import com.manruhomerun.yadanbeopseok.network.common.extension.requireData
import com.manruhomerun.yadanbeopseok.network.travel.api.TravelSpotApi
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotPageResponseDto
import javax.inject.Inject

/**
 * 관광지 조회, 검색, 추천 및 찜 API의 Repository 구현체입니다.
 */
internal class TravelSpotRepositoryImpl @Inject constructor(
    private val travelSpotApi: TravelSpotApi,
    private val apiCallExecutor: ApiCallExecutor,
) : TravelSpotRepository {
    override suspend fun getPopularTravelSpots(
        region: Region,
        category: TravelSpotFilterCategory,
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

    override suspend fun getSuggestedTravelSpots(region: Region): List<TravelSpot> {
        val response = apiCallExecutor.execute {
            travelSpotApi.getSuggestedTravelSpots(region = region)
        }

        return response.requireData().map { dto ->
            dto.toTravelSpot()
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

    override suspend fun getTravelSpotDibs(
        region: Region,
        category: TravelSpotFilterCategory,
    ): List<TravelSpot> {
        val firstPage = getTravelSpotDibsPage(
            region = region,
            category = category,
            pageNumber = FIRST_PAGE_NUMBER,
        )

        val remainingPageNumbers = (FIRST_PAGE_NUMBER + 1)..firstPage.totalPages
        val remainingSpots = if (firstPage.totalPages > FIRST_PAGE_NUMBER) {
            remainingPageNumbers.flatMap { pageNumber ->
                getTravelSpotDibsPage(
                    region = region,
                    category = category,
                    pageNumber = pageNumber,
                ).contents
            }
        } else {
            emptyList()
        }

        return (firstPage.contents + remainingSpots)
            .distinctBy { dto -> dto.id }
            .map { dto -> dto.toTravelSpot(defaultDibs = true) }
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

    /** 찜 목록의 한 페이지를 서버에서 조회합니다. */
    private suspend fun getTravelSpotDibsPage(
        region: Region,
        category: TravelSpotFilterCategory,
        pageNumber: Int,
    ): TravelSpotPageResponseDto =
        apiCallExecutor.execute {
            travelSpotApi.getTravelSpotDibs(
                region = region,
                category = category,
                pageNumber = pageNumber,
                pageSize = DIBS_PAGE_SIZE,
            )
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

private const val FIRST_PAGE_NUMBER = 1
private const val DIBS_PAGE_SIZE = 10

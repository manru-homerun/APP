package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.data.repository.SuggestTravelSpotsParams
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.model.TravelSpotDetail
import com.manruhomerun.yadanbeopseok.model.TravelSpotListPage
import com.manruhomerun.yadanbeopseok.network.travel.dto.PopularTravelSpotResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotDetailResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotPageResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotSuggestionRequestDto

/** 맞춤 관광지 추천 조건을 서버 요청 DTO로 변환합니다. */
internal fun SuggestTravelSpotsParams.toTravelSpotSuggestionRequestDto() =
    TravelSpotSuggestionRequestDto(
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        regionCode = region.legalDongCode,
        companionConditions = companionConditions.map { condition -> condition.toRequestValue() },
        companionCount = companionCount,
        theme = themeId.toRequestId("themeId"),
        travelSpotIdList = travelSpotIds.map { spotId -> spotId.toRequestId("travelSpotId") },
    )

/**
 * 관광지 응답 DTO를 앱 내부 관광지 모델로 변환합니다.
 *
 * @param defaultDibs 응답에 찜 여부가 없을 때 사용할 기본값
 */
internal fun TravelSpotResponseDto.toTravelSpot(
    defaultDibs: Boolean = false,
): TravelSpot =
    TravelSpot(
        id = id,
        name = name,
        address = address,
        region =
            regionCode?.let { code ->
                Region.findByLegalDongCode(code)
            },
        category = category.toTravelSpotCategory(),
        imageUrl = image,
        dibs = dibs ?: defaultDibs,
    )

/** 관광지 페이지 응답을 앱 내부 페이지 모델로 변환합니다. */
internal fun TravelSpotPageResponseDto.toTravelSpotListPage(
    defaultDibs: Boolean = false,
): TravelSpotListPage =
    TravelSpotListPage(
        travelSpots = contents.map { response ->
            response.toTravelSpot(defaultDibs = defaultDibs)
        },
        pageNumber = pageNumber,
        pageSize = pageSize,
        totalElements = totalElements,
        totalPages = totalPages,
    )

/**
 * 홈 인기 관광지 응답을 앱 내부 관광지 모델로 변환합니다.
 *
 * 인기 관광지는 요청한 시도 단위로 조회하므로 앱의 지역에는 요청 지역을 사용합니다.
 */
internal fun PopularTravelSpotResponseDto.toTravelSpot(region: Region): TravelSpot =
    TravelSpot(
        id = id,
        name = name,
        address = address,
        region = region,
        category = category.toTravelSpotCategory(),
        imageUrl = image,
        dibs = dibs,
    )

/**
 * 관광지 상세 응답과 이미지 목록을 앱 내부 상세 모델로 변환합니다.
 *
 * 이미지 목록의 첫 번째 이미지는 기존 [TravelSpot]의 대표 이미지로도 사용합니다.
 */
internal fun TravelSpotDetailResponseDto.toTravelSpotDetail(
    imageUrls: List<String>,
): TravelSpotDetail {
    val normalizedImageUrls = imageUrls.mapNotNull { imageUrl ->
        imageUrl.trim().takeIf { it.isNotEmpty() }
    }.distinct()

    return TravelSpotDetail(
        spot = TravelSpot(
            id = id,
            name = name,
            address = address,
            region = regionCode?.let { code ->
                Region.findByLegalDongCode(code)
            },
            category = category.toTravelSpotCategory(),
            imageUrl = normalizedImageUrls.firstOrNull(),
            dibs = dibs,
        ),
        telephone = tel?.trim()?.takeIf { it.isNotEmpty() },
        homepage = homepage?.trim()?.takeIf { it.isNotEmpty() },
        longitude = longitude?.trim()?.toDoubleOrNull(),
        latitude = latitude?.trim()?.toDoubleOrNull(),
        overview = overview?.trim()?.takeIf { it.isNotEmpty() },
        imageUrls = normalizedImageUrls,
    )
}

/**
 * 서버의 관광지 카테고리 코드 또는 한글 이름을 앱 카테고리로 변환합니다.
 *
 * 지원하지 않는 값은 기타 카테고리로 처리합니다.
 */
internal fun String.toTravelSpotCategory(): TravelSpotCategory {
    val normalizedCategory = trim()

    return when (normalizedCategory.uppercase()) {
        "TOURIST_ATTRACTION" -> TravelSpotCategory.TOURIST_ATTRACTION
        "CULTURAL_FACILITY", "문화시설" -> TravelSpotCategory.CULTURE
        "FESTIVAL_PERFORMANCE_EVENT", "축제/공연/행사" -> TravelSpotCategory.FESTIVAL
        "TRAVEL_COURSE" -> TravelSpotCategory.TRAVEL_COURSE
        "LEPORTS", "레포츠" -> TravelSpotCategory.LEISURE
        "ACCOMMODATION" -> TravelSpotCategory.ACCOMMODATION
        "SHOPPING" -> TravelSpotCategory.SHOPPING
        "RESTAURANT", "음식점" -> TravelSpotCategory.FOOD
        else ->
            TravelSpotCategory.entries.firstOrNull { category ->
                category.name.equals(
                    other = normalizedCategory,
                    ignoreCase = true,
                ) || category.displayName == normalizedCategory
            } ?: TravelSpotCategory.UNKNOWN
    }
}

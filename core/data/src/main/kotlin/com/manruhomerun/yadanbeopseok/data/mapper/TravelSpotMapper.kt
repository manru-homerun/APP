package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelSpotCategory
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelSpotResponseDto

/**
 * 관광지 응답 DTO를 앱 내부 관광지 모델로 변환합니다.
 *
 * @param defaultDibs 응답에 찜 여부가 없을 때 사용할 기본값
 */
internal fun TravelSpotResponseDto.toTravelSpot(
    defaultDibs: Boolean = false,
): TravelSpot =
    TravelSpot(
        id = id.toString(),
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

/**
 * 서버의 관광지 카테고리 코드 또는 한글 이름을 앱 카테고리로 변환합니다.
 *
 * 지원하지 않는 값은 기타 카테고리로 처리합니다.
 */
private fun String.toTravelSpotCategory(): TravelSpotCategory {
    val normalizedCategory = trim()

    return TravelSpotCategory.entries.firstOrNull { category ->
        category.name.equals(
            other = normalizedCategory,
            ignoreCase = true,
        ) || category.displayName == normalizedCategory
    } ?: TravelSpotCategory.UNKNOWN
}

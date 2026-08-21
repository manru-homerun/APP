package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.data.repository.CreateTravelParams
import com.manruhomerun.yadanbeopseok.data.repository.GenerateTravelCourseParams
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.Travel
import com.manruhomerun.yadanbeopseok.model.TravelBaseballGame
import com.manruhomerun.yadanbeopseok.model.TravelCourse
import com.manruhomerun.yadanbeopseok.model.TravelDay
import com.manruhomerun.yadanbeopseok.model.TravelListPage
import com.manruhomerun.yadanbeopseok.model.TravelPlace
import com.manruhomerun.yadanbeopseok.model.TravelSpot
import com.manruhomerun.yadanbeopseok.model.TravelStatus
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import com.manruhomerun.yadanbeopseok.model.TravelTheme
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelBaseballGameRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelCourseGenerateRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelCourseResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelCreateRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelDetailResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelListResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelScheduleDayRequestDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelThemeResponseDto
import kotlinx.datetime.LocalDate

/**
 * 여행 목록 API 응답을 앱 내부 페이지 모델로 변환합니다.
 */
internal fun TravelListResponseDto.toTravelListPage(): TravelListPage =
    TravelListPage(
        travels = content.map { it.toTravelSummary() },
        pageNumber = pageNumber,
        pageSize = pageSize,
        totalElements = totalElements,
        totalPages = totalPages,
    )

/**
 * 여행 테마 응답 DTO를 앱 내부 여행 테마 모델로 변환합니다.
 */
internal fun TravelThemeResponseDto.toTravelTheme(): TravelTheme =
    TravelTheme(
        id = id.toString(),
        name = name,
    )

/**
 * 여행 코스 생성 응답을 저장 전 여행 코스 모델로 변환합니다.
 *
 * 서버가 반환한 관광지 배열 순서를 각 일차의 방문 순서로 사용합니다.
 * 생성 직후에는 방문 인증 정보가 없으므로 [TravelPlace]의 기본값을 사용합니다.
 */
internal fun TravelCourseResponseDto.toTravelCourse(): TravelCourse =
    TravelCourse(
        baseballGame = TravelBaseballGame(
            id = baseballGame.id.toString(),
            day = baseballGame.day,
            baseballGameAfterIdx = baseballGame.baseballGameAfterIdx,
        ),
        days = days.map { dayResponse ->
            TravelDay(
                day = dayResponse.day,
                places = dayResponse.spots.mapIndexed { index, spotResponse ->
                    TravelPlace(
                        spot = spotResponse.toTravelSpot(),
                        order = index + 1,
                    )
                },
            )
        },
    )

/**
 * 앱 내부의 여행 코스 생성 조건을 서버 요청 DTO로 변환합니다.
 */
internal fun GenerateTravelCourseParams.toTravelCourseGenerateRequestDto() =
    TravelCourseGenerateRequestDto(
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        baseballGameId = baseballGameId.toRequestId("baseballGameId"),
        regionCode = region.legalDongCode,
        friends = friendNicknames,
        theme = themeIds.map { themeId ->
            themeId.toRequestId("themeId")
        },
        travelSpotIdList = travelSpotIds.map { travelSpotId ->
            travelSpotId.toRequestId("travelSpotId")
        },
    )

/**
 * 앱 내부의 최종 여행 정보를 서버 저장 요청 DTO로 변환합니다.
 *
 * 일차와 관광지 순서는 각 모델의 day와 order 값을 기준으로 정렬합니다.
 */
internal fun CreateTravelParams.toTravelCreateRequestDto() =
    TravelCreateRequestDto(
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        baseballGame = TravelBaseballGameRequestDto(
            id = course.baseballGame.id.toRequestId("baseballGameId"),
            day = course.baseballGame.day,
            baseballGameAfterIdx = course.baseballGame.baseballGameAfterIdx,
        ),
        name = name,
        regionCode = region.legalDongCode,
        friends = friendNicknames,
        theme = themeIds.map { themeId ->
            themeId.toRequestId("themeId")
        },
        schedule = course.days
            .sortedBy { travelDay -> travelDay.day }
            .map { travelDay ->
                TravelScheduleDayRequestDto(
                    day = travelDay.day,
                    travelSpotIdList = travelDay.places
                        .sortedBy { place -> place.order }
                        .map { place ->
                            place.spot.id.toRequestId("travelSpotId")
                        },
                )
            },
    )

/**
 * 여행 상세 응답을 앱 내부 여행 모델로 변환합니다.
 *
 * @param travelId 상세 조회에 사용한 여행 ID
 * @param currentDate 여행 상태를 계산할 기준 날짜
 */
internal fun TravelDetailResponseDto.toTravel(
    travelId: String,
    currentDate: LocalDate,
): Travel {
    val parsedStartDate = startDate.toLocalDate("from")
    val parsedEndDate = endDate.toLocalDate("to")

    if (parsedEndDate < parsedStartDate) {
        throw InvalidResponseException(
            message = "Travel end date is earlier than start date.",
        )
    }

    val travelRegion = regionCode.toString().toRegion()

    val travelDays = days.map { dayResponse ->
        val places = dayResponse.travelSpotList.mapIndexed { index, spotResponse ->
            TravelPlace(
                spot = TravelSpot(
                    id = spotResponse.id.toString(),
                    name = spotResponse.name,
                    region = travelRegion,
                    category = spotResponse.category.toTravelSpotCategory(),
                    imageUrl = spotResponse.image,
                ),
                order = index + 1,
                isCertificationTarget = spotResponse.isCertificationTarget,
                isCertified = spotResponse.isCertified,
            )
        }

        TravelDay(
            day = dayResponse.day,
            places = places,
        )
    }

    validateSpotCounts(
        spotsCount = travelDays.sumOf { it.places.size },
        certificationTargetCount = certificationTargetCount,
        certifiedSpotsCount = certifiedSpotsCount,
    )

    return Travel(
        id = travelId,
        startDate = parsedStartDate,
        endDate = parsedEndDate,
        baseballGame = TravelBaseballGame(
            id = baseballGame.id.toString(),
            day = baseballGame.day,
            baseballGameAfterIdx = baseballGame.baseballGameAfterIdx,
        ),
        name = name,
        region = travelRegion,
        friends = friends,
        isLeader = isLeader,
        themeIds = themeIds.map { it.toString() },
        certificationTargetCount = certificationTargetCount,
        certifiedSpotsCount = certifiedSpotsCount,
        days = travelDays,
        status = resolveTravelStatus(
            startDate = parsedStartDate,
            endDate = parsedEndDate,
            currentDate = currentDate,
        ),
    )
}

/**
 * 여행 목록의 개별 응답을 앱 내부 여행 요약 모델로 변환합니다.
 */
private fun TravelResponseDto.toTravelSummary(): TravelSummary {
    val parsedStartDate = startDate.toLocalDate("from")
    val parsedEndDate = endDate.toLocalDate("to")

    if (parsedEndDate < parsedStartDate) {
        throw InvalidResponseException(
            message = "Travel end date is earlier than start date.",
        )
    }

    validateSpotCounts(
        spotsCount = spotsCount,
        certificationTargetCount = certificationTargetCount,
        certifiedSpotsCount = certifiedSpotsCount,
    )

    return TravelSummary(
        id = id.toString(),
        name = name,
        startDate = parsedStartDate,
        endDate = parsedEndDate,
        baseballGameId = baseballGame.id.toString(),
        homeTeam = baseballGame.homeTeamId.toKboTeam("homeTeamId"),
        awayTeam = baseballGame.awayTeamId.toKboTeam("awayTeamId"),
        region = regionCode.toRegion(),
        isLeader = isLeader,
        spotsCount = spotsCount,
        certificationTargetCount = certificationTargetCount,
        certifiedSpotsCount = certifiedSpotsCount,
        hasSticker = hasSticker,
    )
}

/**
 * 전체 장소 수와 방문 인증 수의 정합성을 검증합니다.
 */
private fun validateSpotCounts(
    spotsCount: Int,
    certificationTargetCount: Int,
    certifiedSpotsCount: Int,
) {
    val hasInvalidCount =
        spotsCount < 0 ||
            certificationTargetCount < 0 ||
            certifiedSpotsCount < 0 ||
            certificationTargetCount > spotsCount ||
            certifiedSpotsCount > certificationTargetCount

    if (hasInvalidCount) {
        throw InvalidResponseException(
            message = "Invalid travel spot counts.",
        )
    }
}

/**
 * 여행 기간과 기준 날짜를 사용해 여행 상태를 계산합니다.
 */
private fun resolveTravelStatus(
    startDate: LocalDate,
    endDate: LocalDate,
    currentDate: LocalDate,
): TravelStatus = when {
    currentDate < startDate -> TravelStatus.UPCOMING
    currentDate > endDate -> TravelStatus.COMPLETED
    else -> TravelStatus.ACTIVE
}

/**
 * 서버의 날짜 문자열을 앱의 LocalDate로 변환합니다.
 */
private fun String.toLocalDate(
    fieldName: String,
): LocalDate = try {
    LocalDate.parse(this)
} catch (exception: IllegalArgumentException) {
    throw InvalidResponseException(
        message = "Invalid $fieldName date.",
        cause = exception,
    )
}

/**
 * 서버의 KBO 구단 ID를 앱의 KBO 구단으로 변환합니다.
 */
internal fun Long.toKboTeam(fieldName: String): KboTeam =
    KboTeam.findByServerId(this)
        ?: throw InvalidResponseException(
            message = "Unsupported $fieldName: $this",
        )

/**
 * 서버의 시도 코드를 앱의 야구 여행 지역으로 변환합니다.
 */
private fun String.toRegion(): Region =
    when (this) {
        ProfileRegion.SEOUL.code -> Region.SEOUL
        ProfileRegion.GYEONGGI.code -> Region.SUWON
        ProfileRegion.INCHEON.code -> Region.INCHEON
        ProfileRegion.DAEJEON.code -> Region.DAEJEON
        ProfileRegion.DAEGU.code -> Region.DAEGU
        ProfileRegion.GWANGJU.code -> Region.GWANGJU
        ProfileRegion.BUSAN.code -> Region.BUSAN
        ProfileRegion.GYEONGNAM.code -> Region.CHANGWON

        else -> throw InvalidResponseException(
            message = "Unsupported travel region code: $this",
        )
    }

/**
 * 앱 내부의 문자열 ID를 서버 요청에서 사용하는 숫자 ID로 변환합니다.
 */
private fun String.toRequestId(fieldName: String): Long {
    return toLongOrNull() ?: throw IllegalArgumentException("$fieldName must be numeric.")
}

package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.common.error.InvalidResponseException
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.Region
import com.manruhomerun.yadanbeopseok.model.TravelListPage
import com.manruhomerun.yadanbeopseok.model.TravelSummary
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelListResponseDto
import com.manruhomerun.yadanbeopseok.network.travel.dto.TravelResponseDto
import kotlinx.datetime.LocalDate

/**
 * 여행 목록 API 응답을 앱 내부 페이지 모델로 변환합니다.
 *
 * 네트워크 DTO의 날짜, 숫자 ID, 팀 ID와 시도 코드를
 * 앱에서 사용하는 모델과 타입으로 변환합니다.
 */
internal fun TravelListResponseDto.toTravelListPage(): TravelListPage =
    TravelListPage(
        travels =
            content.map { travel ->
                travel.toTravelSummary()
            },
        pageNumber = pageNumber,
        pageSize = pageSize,
        totalElements = totalElements,
        totalPages = totalPages,
    )

/**
 * 여행 목록의 개별 응답을 앱 내부 여행 요약 모델로 변환합니다.
 */
private fun TravelResponseDto.toTravelSummary(): TravelSummary {
    val parsedStartDate =
        startDate.toLocalDate(
            fieldName = "from",
        )
    val parsedEndDate =
        endDate.toLocalDate(
            fieldName = "to",
        )

    if (parsedEndDate < parsedStartDate) {
        throw InvalidResponseException(
            message = "Travel end date is earlier than start date.",
        )
    }

    validateSpotCounts()

    return TravelSummary(
        id = id.toString(),
        name = name,
        startDate = parsedStartDate,
        endDate = parsedEndDate,
        baseballGameId = baseballGame.id.toString(),
        homeTeam =
            baseballGame.homeTeamId.toKboTeam(
                fieldName = "homeTeamId",
            ),
        awayTeam =
            baseballGame.awayTeamId.toKboTeam(
                fieldName = "awayTeamId",
            ),
        region = regionCode.toRegion(),
        isLeader = isLeader,
        spotsCount = spotsCount,
        certificationTargetCount = certificationTargetCount,
        certifiedSpotsCount = certifiedSpotsCount,
        hasSticker = hasSticker,
    )
}

/**
 * 서버가 전달한 전체 장소 수와 방문 인증 수의 정합성을 검증합니다.
 */
private fun TravelResponseDto.validateSpotCounts() {
    if (
        spotsCount < 0 ||
        certificationTargetCount < 0 ||
        certifiedSpotsCount < 0 ||
        certificationTargetCount > spotsCount ||
        certifiedSpotsCount > certificationTargetCount
    ) {
        throw InvalidResponseException(
            message = "Invalid travel spot counts.",
        )
    }
}

/**
 * 서버의 yyyy-MM-dd 날짜 문자열을 [LocalDate]로 변환합니다.
 *
 * 날짜 형식이 올바르지 않으면 잘못된 서버 응답으로 처리합니다.
 */
private fun String.toLocalDate(
    fieldName: String,
): LocalDate =
    try {
        LocalDate.parse(this)
    } catch (exception: IllegalArgumentException) {
        throw InvalidResponseException(
            message = "Invalid $fieldName date.",
            cause = exception,
        )
    }

/**
 * 서버의 KBO 구단 ID를 앱의 [KboTeam]으로 변환합니다.
 */
private fun Long.toKboTeam(
    fieldName: String,
): KboTeam =
    KboTeam.findByServerId(this)
        ?: throw InvalidResponseException(
            message = "Unsupported $fieldName: $this",
        )

/**
 * 서버의 시도 코드를 앱의 야구 여행 지역으로 변환합니다.
 *
 * 시도 코드 값은 기존 [ProfileRegion]에 정의된 값을 재사용합니다.
 * KBO 원정 여행 지역으로 지원하지 않는 코드가 전달되면
 * 잘못된 서버 응답으로 처리합니다.
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

        else ->
            throw InvalidResponseException(
                message = "Unsupported travel region code: $this",
            )
    }

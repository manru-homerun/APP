package com.manruhomerun.yadanbeopseok.network.travel.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 여행 목록의 개별 여행 응답 DTO입니다.
 *
 * 날짜, 지역 코드, 경기와 여행 진행 정보를 이후 Data 매퍼에서
 * 앱 내부의 여행 모델로 변환합니다.
 *
 * @property id 여행의 고유 식별자
 * @property startDate 여행 시작일(yyyy-MM-dd)
 * @property endDate 여행 종료일(yyyy-MM-dd)
 * @property baseballGame 여행에 포함된 야구 경기 요약 정보
 * @property name 여행 이름
 * @property regionCode 여행 지역의 시도 코드
 * @property isLeader 현재 사용자가 해당 여행의 방장인지 여부
 * @property spotsCount 여행 일정에 포함된 전체 장소 수
 * @property certifiedSpotsCount 현재 사용자가 인증한 관광지 수
 * @property hasSticker 완료된 여행에서 스티커를 획득했는지 여부
 */
@Serializable
data class TravelResponseDto(
    val id: String,
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val baseballGame: TravelBaseballGameResponseDto,
    val name: String,
    val regionCode: String,
    val isLeader: Boolean,
    @SerialName("spotsCnt")
    val spotsCount: Int,
    @SerialName("vertifiedSpotsCnt")
    val certifiedSpotsCount: Int,
    val hasSticker: Boolean,
)


/**
 * 여행 목록 조회 API의 응답 데이터 DTO입니다.
 *
 * @property contents 현재 페이지에 포함된 여행 목록
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 한 페이지에 포함되는 최대 여행 수
 * @property totalElements 전체 여행 수
 * @property totalPages 전체 페이지 수
 */
@Serializable
data class TravelListResponseDto(
    val contents: List<TravelResponseDto>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)


/**
 * 특정 여행의 상세 일정을 조회한 응답 DTO입니다.
 *
 * 여행 ID는 응답에 포함되지 않으므로 요청에 사용한 travelId를
 * Data 계층에서 앱 내부 여행 모델의 ID로 사용합니다.
 */
@Serializable
data class TravelDetailResponseDto(
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val baseballGame: TravelCourseBaseballGameResponseDto,
    val name: String? = null,
    val regionCode: Int,
    val friends: List<String>,
    val isLeader: Boolean,
    @SerialName("theme")
    val themeIds: List<Long>,
    @SerialName("verificationTargetCount")
    val certificationTargetCount: Int,

    @SerialName("verifiedSpotsCount")
    val certifiedSpotsCount: Int,
    @SerialName("schedule")
    val days: List<TravelScheduleDayResponseDto>,
)

/**
 * 여행 코스에서 야구 경기가 배치된 위치 정보입니다.
 *
 * 여행 상세 조회와 코스 생성 응답에서 공통으로 사용합니다.
 */
@Serializable
data class TravelCourseBaseballGameResponseDto(
    val id: Long,
    val day: Int,
    val baseballGameAfterIdx: Int,
)

/**
 * 저장된 여행의 특정 일차와 해당 일차의 관광지 목록입니다.
 */
@Serializable
data class TravelScheduleDayResponseDto(
    val day: Int,
    val travelSpotList: List<TravelScheduleSpotResponseDto>,
)

/**
 * 저장된 여행 일정에 포함된 개별 관광지 응답입니다.
 *
 * 관광지 기본 정보와 현재 사용자의 방문 인증 상태를 포함합니다.
 */
@Serializable
data class TravelScheduleSpotResponseDto(
    val id: Long,
    val name: String,
    val category: String,
    val image: String? = null,
    @SerialName("isVerificationTarget")
    val isCertificationTarget: Boolean,

    @SerialName("isVerified")
    val isCertified: Boolean,
)

/**
 * 여행 코스 생성 및 재정렬 API의 공통 응답 DTO입니다.
 *
 * 생성된 경기 배치 정보와 일차별 추천 일정을 함께 보관합니다.
 */
@Serializable
data class TravelCourseResponseDto(
    val baseballGame: TravelCourseBaseballGameResponseDto,
    @SerialName("schedule")
    val days: List<TravelCourseDayResponseDto>,
)

/**
 * 생성된 여행 코스의 특정 일차와 관광지 목록입니다.
 *
 * 개별 관광지는 기존 [TravelSpotResponseDto]를 재사용합니다.
 */
@Serializable
data class TravelCourseDayResponseDto(
    val day: Int,
    @SerialName("travelSpotList")
    val spots: List<TravelSpotResponseDto>,
)

/**
 * 여행 테마 종류 조회 API의 개별 응답 DTO입니다.
 */
@Serializable
data class TravelThemeResponseDto(
    val id: Long,
    val name: String,
)

/**
 * 관광지 방문 인증 성공 응답의 data 부분입니다.
 *
 * 인증 결과 식별과 완료 화면에 필요한 필드만 수신합니다.
 * 인증 시각은 Mapper에서 앱 내부 날짜 타입으로 변환합니다.
 */
@Serializable
data class TravelSpotVerifyResponseDto(
    val visitVerificationId: Long,
    val travelId: Long,
    val tourSpotId: Long,
    val tourSpotName: String,
    val verifiedAt: String,
)

/**
 * 특정 여행에서 획득한 스티커 조회 응답입니다.
 *
 * 공통 ApiResponseDto로 감싸지 않는 직접 응답입니다.
 * 미획득 상태에서는 hasSticker가 false이고 stickerPack이 null입니다.
 */
@Serializable
data class TravelStickerResponseDto(
    val hasSticker: Boolean,
    val stickerPack: StickerPackResponseDto?,
)

/**
 * 획득한 스티커팩의 정보와 소속 스티커 목록입니다.
 */
@Serializable
data class StickerPackResponseDto(
    val id: Long,
    val name: String,
    val stickers: List<StickerResponseDto>,
)

/**
 * 스티커의 식별자와 이미지 URL입니다.
 *
 * 소속 스티커팩 ID는 Mapper에서 상위 객체의 ID를 사용합니다.
 */
@Serializable
data class StickerResponseDto(
    val id: Long,
    val image: String,
)

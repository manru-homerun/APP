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
 * @property certificationTargetCount 방문 인증 대상 관광지 수
 * @property certifiedSpotsCount 현재 사용자가 인증한 관광지 수
 * @property hasSticker 완료된 여행에서 스티커를 획득했는지 여부
 */
@Serializable
data class TravelResponseDto(
    val id: Long,
    @SerialName("from")
    val startDate: String,
    @SerialName("to")
    val endDate: String,
    val baseballGame: TravelBaseballGameResponseDto,
    val name: String,
    val regionCode: String,
    val isLeader: Boolean,
    val spotsCount: Int,
    val certificationTargetCount: Int,
    val certifiedSpotsCount: Int,
    val hasSticker: Boolean,
)


/**
 * 여행 목록 조회 API의 응답 데이터 DTO입니다.
 *
 * 여행 목록과 함께 서버에서 제공하는 페이지 정보를 보관합니다.
 * 현재는 여행 목록 API에서만 사용하는 구조이므로 여행 도메인에 둡니다.
 *
 * @property content 현재 페이지에 포함된 여행 목록
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 한 페이지에 포함되는 최대 여행 수
 * @property totalElements 전체 여행 수
 * @property totalPages 전체 페이지 수
 */
@Serializable
data class TravelListResponseDto(
    val content: List<TravelResponseDto>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)

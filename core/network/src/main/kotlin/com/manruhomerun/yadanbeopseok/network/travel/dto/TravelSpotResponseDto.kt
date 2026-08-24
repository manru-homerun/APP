package com.manruhomerun.yadanbeopseok.network.travel.dto

import kotlinx.serialization.Serializable

/**
 * 관광지 목록 API에서 공통으로 사용하는 개별 관광지 응답 DTO입니다.
 *
 * 인기 관광지, 사용자 맞춤 추천 관광지, 관광지 검색 및
 * 찜 목록에서 동일한 응답 구조를 재사용합니다.
 *
 * @property id 관광지 고유 식별자이며 찜 요청의 contentId와 같은 값
 * @property name 관광지 이름
 * @property address 관광지 주소
 * @property category 백엔드에서 전달하는 관광지 카테고리
 * @property image 관광지 대표 이미지 URL
 * @property regionCode 관광지가 속한 5자리 지역 코드
 * @property dibs 현재 사용자의 찜 여부이며, 제공하지 않는 API에서는 null
 */
@Serializable
data class TravelSpotResponseDto(
    val id: Long,
    val name: String,
    val address: String? = null,
    val category: String,
    val image: String? = null,
    val regionCode: String? = null,
    val dibs: Boolean? = null,
)

/**
 * 관광지 목록 API에서 공통으로 사용하는 응답 데이터 DTO입니다.
 *
 * 인기 관광지, 사용자 맞춤 추천 관광지 및 관광지 검색 API에서
 * 동일한 목록 구조를 재사용합니다.
 *
 * @property content 응답에 포함된 관광지 목록
 */
@Serializable
data class TravelSpotListResponseDto(
    val content: List<TravelSpotResponseDto>,
)

/**
 * 관광지 검색 API의 페이지 응답 DTO입니다.
 *
 * 검색 결과의 개별 관광지는 기존 [TravelSpotResponseDto]를 재사용합니다.
 *
 * @property contents 현재 페이지의 관광지 검색 결과
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 한 페이지에 포함되는 관광지 수
 * @property totalElements 전체 검색 결과 수
 * @property totalPages 전체 페이지 수
 */
@Serializable
data class TravelSpotSearchResponseDto(
    val contents: List<TravelSpotResponseDto>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)

/**
 * 관광지 상세 조회 API의 응답 데이터입니다.
 *
 * @property id 관광지 고유 식별자
 * @property category 관광지 카테고리
 * @property title 관광지 이름
 * @property tel 관광지 전화번호
 * @property homepage 관광지 홈페이지 정보
 * @property regionCode 관광지가 속한 5자리 지역 코드
 * @property address 관광지 주소
 * @property longitude 관광지 경도 문자열
 * @property latitude 관광지 위도 문자열
 * @property overview 관광지 상세 소개
 * @property dibs 현재 사용자의 찜 여부
 */
@Serializable
data class TravelSpotDetailResponseDto(
    val id: Long,
    val category: String,
    val title: String,
    val tel: String? = null,
    val homepage: String? = null,
    val regionCode: String? = null,
    val address: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val overview: String? = null,
    val dibs: Boolean,
)

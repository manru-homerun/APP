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

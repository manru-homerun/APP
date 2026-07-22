package com.manruhomerun.yadanbeopseok.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * 앱 내부에서 사용하는 여행 모델입니다.
 *
 * 서버 ERD에서는 travel, travel_user_mapping, travel_travel_spot_mapping,
 * travel_certi 등이 나뉘어 있지만, 앱에서는 한 여행 화면을 구성하기 쉽게
 * 참여자, 일차별 장소, 테마를 묶어서 사용합니다.
 */
data class Travel(
    val id: String,
    val name: String?,
    val baseballGame: BaseballGame,
    val region: Region,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val participants: List<TravelParticipant>,
    val days: List<TravelDay>,
    val themes: List<TravelTheme> = emptyList(),
    val status: TravelStatus,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

data class TravelParticipant(
    val user: UserProfile,
    val isLeader: Boolean,
)

data class TravelDay(
    val day: Int,
    val places: List<TravelPlace>,
)

data class TravelPlace(
    val id: String,
    val spot: TravelSpot,
    val day: Int,
    val order: Int,
    val certifications: List<TravelCertification> = emptyList(),
) {
    /**
     * 특정 사용자가 이 장소를 방문 인증했는지 확인합니다.
     */
    fun isCertifiedBy(userId: String): Boolean =
        certifications.any { certification ->
            certification.userId == userId
        }
}

/**
 * 여행지 방문 인증 정보입니다.
 *
 * ERD의 travel_certi를 앱에서 쓰기 좋게 정리한 모델입니다.
 */
data class TravelCertification(
    val id: String,
    val userId: String,
    val certificatedAt: LocalDateTime,
)

enum class TravelStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED,
}

data class TravelTheme(
    val id: String,
    val code: String,
    val name: String,
    val displayOrder: Int,
)

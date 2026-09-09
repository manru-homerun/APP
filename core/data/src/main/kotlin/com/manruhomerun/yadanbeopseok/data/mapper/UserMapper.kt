package com.manruhomerun.yadanbeopseok.data.mapper

import com.manruhomerun.yadanbeopseok.common.InvalidResponseException
import com.manruhomerun.yadanbeopseok.model.Gender
import com.manruhomerun.yadanbeopseok.model.KboTeam
import com.manruhomerun.yadanbeopseok.model.ProfileRegion
import com.manruhomerun.yadanbeopseok.model.TravelPreference
import com.manruhomerun.yadanbeopseok.model.TravelStyleScore
import com.manruhomerun.yadanbeopseok.model.UserProfile
import com.manruhomerun.yadanbeopseok.network.user.dto.TravelPreferenceResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.TravelPreferenceUpdateRequestDto
import com.manruhomerun.yadanbeopseok.network.user.dto.UserProfileResponseDto
import com.manruhomerun.yadanbeopseok.network.user.dto.UserProfileUpdateRequestDto
import kotlinx.datetime.LocalDate

/**
 * 사용자 프로필 응답을 앱 내부 모델로 변환합니다.
 */
internal fun UserProfileResponseDto.toUserProfile(): UserProfile =
    UserProfile(
        id = userId,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        favoriteTeam = favoriteTeam.teamId.toKboTeam("favoriteTeam.teamId"),
        gender = gender.toGender(),
        birthDate = birthday.toBirthDate(),
    )

/**
 * 여행 취향 응답을 앱 내부 모델로 변환합니다.
 */
internal fun TravelPreferenceResponseDto.toTravelPreference(): TravelPreference {
    val mappedResidenceRegion =
        residenceRegion.regionCode.toProfileRegion("residenceRegion.regionCode")

    val mappedPreferredRegions =
        preferredRegions.mapIndexed { index, region ->
            region.regionCode.toProfileRegion("preferredRegions[$index].regionCode")
        }

    if (!mappedResidenceRegion.isAvailableForResidence) {
        throw InvalidResponseException(
            message = "Unavailable residence region: ${mappedResidenceRegion.code}",
        )
    }

    if (mappedPreferredRegions.isEmpty()) {
        throw InvalidResponseException(
            message = "Preferred regions must not be empty.",
        )
    }

    if (mappedPreferredRegions.any { !it.isAvailableForPreferredTravel }) {
        throw InvalidResponseException(
            message = "Preferred regions contain an unavailable region.",
        )
    }

    return TravelPreference(
        travelStyleScore = travelStyleValue.toTravelStyleScore(),
        residenceRegion = mappedResidenceRegion,
        preferredTravelRegions = mappedPreferredRegions,
    )
}

/**
 * 현재 프로필에 전달받은 변경값을 반영하여 전체 수정 요청을 만듭니다.
 */
internal fun UserProfile.toUserProfileUpdateRequestDto(
    updatedNickname: String?,
    updatedProfileImageUrl: String?,
    updatedFavoriteTeam: KboTeam?,
): UserProfileUpdateRequestDto {
    val currentNickname =
        nickname ?: throw InvalidResponseException(
            message = "Current user profile is missing nickname.",
        )

    val currentFavoriteTeam =
        favoriteTeam ?: throw InvalidResponseException(
            message = "Current user profile is missing favoriteTeam.",
        )

    val currentBirthDate =
        birthDate ?: throw InvalidResponseException(
            message = "Current user profile is missing birthDate.",
        )

    val currentGender =
        gender ?: throw InvalidResponseException(
            message = "Current user profile is missing gender.",
        )

    return UserProfileUpdateRequestDto(
        profileImageUrl = updatedProfileImageUrl ?: profileImageUrl,
        nickname = updatedNickname ?: currentNickname,
        favoriteTeamId = (updatedFavoriteTeam ?: currentFavoriteTeam).serverId,
        birthday = currentBirthDate.toString(),
        gender = currentGender.toRequestCode(),
    )
}

/**
 * 앱 내부 여행 취향을 수정 요청 DTO로 변환합니다.
 */
internal fun TravelPreference.toTravelPreferenceUpdateRequestDto() =
    TravelPreferenceUpdateRequestDto(
        residenceRegion = residenceRegion.displayName,
        travelStyleValue = travelStyleScore.value,
        preferredRegions = preferredTravelRegions.map { region ->
            region.displayName
        },
    )

private fun String.toGender(): Gender =
    Gender.entries.firstOrNull { gender ->
        gender.name == this
    } ?: throw InvalidResponseException(
        message = "Unsupported user gender: $this",
    )

private fun String.toBirthDate(): LocalDate =
    try {
        LocalDate.parse(this)
    } catch (exception: IllegalArgumentException) {
        throw InvalidResponseException(
            message = "Invalid user birthday: $this",
            cause = exception,
        )
    }

private fun String.toProfileRegion(fieldName: String): ProfileRegion {
    val normalizedCode = if (length == 5) take(2) else this

    return ProfileRegion.findByCode(normalizedCode)
        ?: throw InvalidResponseException(
            message = "Unsupported $fieldName: $this",
        )
}

private fun Int.toTravelStyleScore(): TravelStyleScore =
    try {
        TravelStyleScore(this)
    } catch (exception: IllegalArgumentException) {
        throw InvalidResponseException(
            message = "Invalid travelStyleValue: $this",
            cause = exception,
        )
    }
